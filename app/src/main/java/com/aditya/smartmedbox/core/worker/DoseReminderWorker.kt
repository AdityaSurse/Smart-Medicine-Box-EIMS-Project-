package com.aditya.smartmedbox.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.notification.DoseNotificationManager
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.history.domain.usecase.LogDoseUseCase
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class DoseReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val doseScheduleRepository: DoseScheduleRepository,
    private val doseEventRepository: DoseEventRepository,
    private val logDoseUseCase: LogDoseUseCase,
    private val notificationManager: DoseNotificationManager,
    private val deviceStatusDao: DeviceStatusDao,
    private val medicineDao: MedicineDao
) : CoroutineWorker(context, params) {

    companion object {
        private const val PERIODIC_WORK_NAME = "dose_audit_periodic"
        private const val IMMEDIATE_WORK_NAME = "dose_audit_immediate"
        private const val OFFLINE_THRESHOLD_MILLIS = 30 * 60 * 1000L
        private const val MAX_SAFE_TEMP_CELSIUS = 30.0f
        private const val MIN_SAFE_TEMP_CELSIUS = 10.0f
        private const val MAX_SAFE_HUMIDITY_PERCENT = 70.0f

        fun enqueuePeriodicAudit(context: Context) {
            val periodicRequest = PeriodicWorkRequestBuilder<DoseReminderWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }

        fun enqueueImmediateAudit(context: Context) {
            val oneTimeRequest = OneTimeWorkRequestBuilder<DoseReminderWorker>()
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
        }
    }

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        auditDoseSchedules(now)
        auditDeviceVitals(now)
        auditMedicineStock()
        return Result.success()
    }

    private suspend fun auditDoseSchedules(now: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = now }

        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dayOfWeekIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val schedules = doseScheduleRepository.getSchedules().firstOrNull() ?: emptyList()
        val todayEvents = doseEventRepository.getEventsForDay(startOfDay, endOfDay).firstOrNull() ?: emptyList()
        val existingEventsByKey = todayEvents.associateBy { "${it.scheduleId}_${it.scheduledTimeEpochMillis}" }

        val activeSchedules = schedules.filter { it.isActive && it.daysOfWeek.contains(dayOfWeekIndex) }

        for (schedule in activeSchedules) {
            auditSchedule(schedule, startOfDay, now, existingEventsByKey)
        }
    }

    private suspend fun auditDeviceVitals(now: Long) {
        val status = deviceStatusDao.getDeviceStatus("DG_BOX_4081").firstOrNull() ?: return

        // 1. Alert on device offline > 30 minutes
        if (now - status.lastSeenTimestamp > OFFLINE_THRESHOLD_MILLIS) {
            notificationManager.showDeviceAlertNotification(
                notificationId = "device_offline".hashCode(),
                title = "Smart Medicine Box Offline",
                message = "Smart Box #DG-4081 has been unreachable for over 30 minutes. Check Wi-Fi or battery."
            )
        }

        // 2. Alert on temperature out of safe range
        if (status.temperatureCelsius > MAX_SAFE_TEMP_CELSIUS || status.temperatureCelsius < MIN_SAFE_TEMP_CELSIUS) {
            notificationManager.showEnvironmentalAlertNotification(
                notificationId = "temp_alert".hashCode(),
                title = "Chamber Temperature Warning",
                message = "Box temperature (${status.temperatureCelsius}°C) is outside safe range (10°C - 30°C)."
            )
        }

        // 3. Alert on humidity out of safe range
        if (status.humidityPercent > MAX_SAFE_HUMIDITY_PERCENT) {
            notificationManager.showEnvironmentalAlertNotification(
                notificationId = "humidity_alert".hashCode(),
                title = "Chamber Humidity Alert",
                message = "Box humidity (${status.humidityPercent.toInt()}%) exceeds safe dry-seal threshold (70%)."
            )
        }
    }

    private suspend fun auditMedicineStock() {
        val medicines = medicineDao.getAllMedicines().firstOrNull() ?: emptyList()
        for (medicine in medicines) {
            if (medicine.currentStock <= medicine.lowStockThreshold) {
                notificationManager.showLowStockNotification(
                    notificationId = medicine.id.hashCode(),
                    medicineName = medicine.name,
                    stockRemaining = medicine.currentStock
                )
            }
        }
    }

    private suspend fun auditSchedule(schedule: DoseSchedule, startOfDay: Long, now: Long, existingEventsByKey: Map<String, DoseEvent>) {
        for (timeStr in schedule.times) {
            val scheduledEpoch = parseTimeToEpochToday(timeStr, startOfDay)
            val key = "${schedule.id}_$scheduledEpoch"
            val existing = existingEventsByKey[key]

            val gracePeriodMillis = schedule.gracePeriodMinutes * 60 * 1000L
            val isGraceExpired = now > (scheduledEpoch + gracePeriodMillis)

            if (existing != null) {
                auditExistingEvent(existing, schedule, isGraceExpired, now)
            } else if (isGraceExpired) {
                recordUnregisteredMissedDose(schedule, scheduledEpoch, now)
            }
        }
    }

    private suspend fun auditExistingEvent(existing: DoseEvent, schedule: DoseSchedule, isGraceExpired: Boolean, now: Long) {
        if (existing.status == DoseStatus.PENDING && isGraceExpired) {
            logDoseUseCase(
                eventId = existing.id,
                status = DoseStatus.MISSED,
                loggedTimestamp = now
            )
            notificationManager.showMissedDoseNotification(
                notificationId = existing.id.hashCode(),
                scheduleId = schedule.id,
                medicineName = schedule.medicineName,
                dosage = "1 tablet"
            )
        }
    }

    private suspend fun recordUnregisteredMissedDose(schedule: DoseSchedule, scheduledEpoch: Long, now: Long) {
        val eventId = "evt_${schedule.id}_$scheduledEpoch"
        val missedEvent = DoseEvent(
            id = eventId,
            scheduleId = schedule.id,
            medicineId = schedule.medicineId,
            medicineName = schedule.medicineName,
            dosage = "1 tablet",
            scheduledTimeEpochMillis = scheduledEpoch,
            status = DoseStatus.MISSED,
            loggedTimeEpochMillis = now,
            bayLabel = schedule.compartmentBay
        )
        doseEventRepository.insertDoseEvent(missedEvent)
        notificationManager.showMissedDoseNotification(
            notificationId = eventId.hashCode(),
            scheduleId = schedule.id,
            medicineName = schedule.medicineName,
            dosage = "1 tablet"
        )
    }

    private fun parseTimeToEpochToday(timeStr: String, startOfDayMillis: Long): Long {
        val parts = timeStr.trim().split(" ", ":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val amPm = parts.getOrNull(2) ?: if (timeStr.contains("PM", ignoreCase = true)) "PM" else "AM"

        if (amPm.equals("PM", ignoreCase = true) && hour < 12) hour += 12
        if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

        return startOfDayMillis + (hour * 3600 + minute * 60) * 1000L
    }
}
