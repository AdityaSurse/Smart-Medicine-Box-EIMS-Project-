package com.aditya.smartmedbox.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.ACTION_DOSE_ALARM
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_DOSAGE
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_DOSE_EPOCH
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_GRACE_PERIOD_MINUTES
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_MEDICINE_NAME
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_REPEAT_COUNT
import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler.Companion.EXTRA_SCHEDULE_ID
import com.aditya.smartmedbox.core.notification.DoseNotificationManager
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.history.domain.usecase.LogDoseUseCase
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DoseAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var doseEventRepository: DoseEventRepository

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    @Inject
    lateinit var alarmScheduler: DoseAlarmScheduler

    @Inject
    lateinit var notificationManager: DoseNotificationManager

    @Inject
    lateinit var logDoseUseCase: LogDoseUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DOSE_ALARM) return

        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: "1 tablet"
        val doseEpochMillis = intent.getLongExtra(EXTRA_DOSE_EPOCH, System.currentTimeMillis())
        val gracePeriodMinutes = intent.getIntExtra(EXTRA_GRACE_PERIOD_MINUTES, 30)
        val repeatCount = intent.getIntExtra(EXTRA_REPEAT_COUNT, 0)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleAlarmTrigger(
                    scheduleId = scheduleId,
                    medicineName = medicineName,
                    dosage = dosage,
                    doseEpochMillis = doseEpochMillis,
                    gracePeriodMinutes = gracePeriodMinutes,
                    repeatCount = repeatCount
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleAlarmTrigger(scheduleId: String, medicineName: String, dosage: String, doseEpochMillis: Long, gracePeriodMinutes: Int, repeatCount: Int) {
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

        val todayEvents = doseEventRepository.getEventsForDay(startOfDay, endOfDay).firstOrNull() ?: emptyList()
        val matchingEvent = todayEvents.firstOrNull {
            it.scheduleId == scheduleId && it.scheduledTimeEpochMillis == doseEpochMillis
        }

        // If dose already taken, cancel alarms and do nothing
        if (matchingEvent?.status == DoseStatus.TAKEN) {
            alarmScheduler.cancelReminder(scheduleId)
            return
        }

        val gracePeriodMillis = gracePeriodMinutes * 60 * 1000L
        val isGraceExpired = now > (doseEpochMillis + gracePeriodMillis)

        val notificationId = (scheduleId + doseEpochMillis).hashCode()

        if (isGraceExpired) {
            val eventId = matchingEvent?.id ?: "evt_${scheduleId}_$doseEpochMillis"
            if (matchingEvent != null) {
                logDoseUseCase(
                    eventId = matchingEvent.id,
                    status = DoseStatus.MISSED,
                    loggedTimestamp = now
                )
            } else {
                val missedEvent = DoseEvent(
                    id = eventId,
                    scheduleId = scheduleId,
                    medicineId = scheduleId,
                    medicineName = medicineName,
                    dosage = dosage,
                    scheduledTimeEpochMillis = doseEpochMillis,
                    status = DoseStatus.MISSED,
                    loggedTimeEpochMillis = now
                )
                doseEventRepository.insertDoseEvent(missedEvent)
            }

            notificationManager.showMissedDoseNotification(
                notificationId = notificationId,
                scheduleId = scheduleId,
                medicineName = medicineName,
                dosage = dosage
            )
            alarmScheduler.cancelReminder(scheduleId)
            return
        }

        // Dose is still pending: trigger high-priority dose notification
        notificationManager.showDoseReminderNotification(
            notificationId = notificationId,
            scheduleId = scheduleId,
            medicineName = medicineName,
            dosage = dosage,
            repeatCount = repeatCount
        )

        // Repeating reminder loop capped by user settings and grace period
        val userSettings = userSettingsRepository.getUserSettings().firstOrNull()
        val maxRepeats = userSettings?.maxReminderRepeats ?: 3
        val snoozeIntervalMinutes = userSettings?.snoozeIntervalMinutes ?: 10

        val nextRepeatCount = repeatCount + 1
        val nextTriggerEpoch = now + (snoozeIntervalMinutes * 60 * 1000L)

        if (nextRepeatCount < maxRepeats && nextTriggerEpoch < (doseEpochMillis + gracePeriodMillis)) {
            alarmScheduler.scheduleReminder(
                scheduleId = scheduleId,
                medicineName = medicineName,
                dosage = dosage,
                doseEpochMillis = nextTriggerEpoch,
                gracePeriodMinutes = gracePeriodMinutes,
                repeatCount = nextRepeatCount
            )
        }
    }
}
