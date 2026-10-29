package com.aditya.smartmedbox.core.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class AndroidDoseAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val doseScheduleRepository: DoseScheduleRepository,
    private val doseEventRepository: DoseEventRepository
) : DoseAlarmScheduler {

    companion object {
        const val ACTION_DOSE_ALARM = "com.aditya.smartmedbox.ACTION_DOSE_ALARM"
        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_DOSE_EPOCH = "extra_dose_epoch"
        const val EXTRA_GRACE_PERIOD_MINUTES = "extra_grace_period_minutes"
        const val EXTRA_REPEAT_COUNT = "extra_repeat_count"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(scheduleId: String, medicineName: String, dosage: String, doseEpochMillis: Long, gracePeriodMinutes: Int, repeatCount: Int) {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            action = ACTION_DOSE_ALARM
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(EXTRA_MEDICINE_NAME, medicineName)
            putExtra(EXTRA_DOSAGE, dosage)
            putExtra(EXTRA_DOSE_EPOCH, doseEpochMillis)
            putExtra(EXTRA_GRACE_PERIOD_MINUTES, gracePeriodMinutes)
            putExtra(EXTRA_REPEAT_COUNT, repeatCount)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.hashCode() + repeatCount,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        doseEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        doseEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    doseEpochMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                doseEpochMillis,
                pendingIntent
            )
        }
    }

    override fun cancelReminder(scheduleId: String) {
        val intent = Intent(context, DoseAlarmReceiver::class.java).apply {
            action = ACTION_DOSE_ALARM
        }
        for (i in 0..10) {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.hashCode() + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    override suspend fun rescheduleAllActiveReminders() {
        val now = System.currentTimeMillis()
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

        val dayOfWeekIndex = when (Calendar.getInstance().apply { timeInMillis = now }.get(Calendar.DAY_OF_WEEK)) {
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
        val eventsByKey = todayEvents.associateBy { "${it.scheduleId}_${it.scheduledTimeEpochMillis}" }

        val activeSchedules = schedules.filter { it.isActive && it.daysOfWeek.contains(dayOfWeekIndex) }

        for (schedule in activeSchedules) {
            rescheduleForSchedule(schedule, startOfDay, now, eventsByKey)
        }
    }

    private fun rescheduleForSchedule(schedule: DoseSchedule, startOfDay: Long, now: Long, eventsByKey: Map<String, DoseEvent>) {
        for (timeStr in schedule.times) {
            val scheduledEpoch = parseTimeToEpochToday(timeStr, startOfDay)
            val key = "${schedule.id}_$scheduledEpoch"
            val existing = eventsByKey[key]

            val gracePeriodMillis = schedule.gracePeriodMinutes * 60 * 1000L
            val isPastGrace = now > (scheduledEpoch + gracePeriodMillis)

            if (existing?.status != DoseStatus.TAKEN && !isPastGrace) {
                val triggerTime = if (scheduledEpoch > now) scheduledEpoch else now + 1_000L
                scheduleReminder(
                    scheduleId = schedule.id,
                    medicineName = schedule.medicineName,
                    dosage = "1 tablet",
                    doseEpochMillis = triggerTime,
                    gracePeriodMinutes = schedule.gracePeriodMinutes,
                    repeatCount = 0
                )
            }
        }
    }

    override fun canScheduleExactAlarms(): Boolean {
        return ExactAlarmPermissionHelper.canScheduleExactAlarms(context)
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
