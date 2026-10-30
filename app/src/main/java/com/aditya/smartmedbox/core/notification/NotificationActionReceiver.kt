package com.aditya.smartmedbox.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.usecase.LogDoseUseCase
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_AS_TAKEN = "com.aditya.smartmedbox.ACTION_MARK_AS_TAKEN"
        const val ACTION_SNOOZE_DOSE = "com.aditya.smartmedbox.ACTION_SNOOZE_DOSE"

        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_MEDICINE_NAME = "extra_medicine_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_GRACE_PERIOD_MINUTES = "extra_grace_period_minutes"
        const val EXTRA_REPEAT_COUNT = "extra_repeat_count"
        const val DEFAULT_SNOOZE_MINUTES = 10
    }

    @Inject
    lateinit var logDoseUseCase: LogDoseUseCase

    @Inject
    lateinit var notificationManager: DoseNotificationManager

    @Inject
    lateinit var alarmScheduler: DoseAlarmScheduler

    @Inject
    lateinit var userSettingsRepository: UserSettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_MARK_AS_TAKEN -> handleMarkAsTaken(intent, scheduleId, notificationId)
                    ACTION_SNOOZE_DOSE -> handleSnoozeDose(intent, scheduleId, notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleMarkAsTaken(intent: Intent, scheduleId: String, notificationId: Int) {
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: "evt_${scheduleId}_${System.currentTimeMillis()}"
        logDoseUseCase(
            eventId = eventId,
            status = DoseStatus.TAKEN,
            loggedTimestamp = System.currentTimeMillis()
        )
        notificationManager.cancelNotification(notificationId)
        alarmScheduler.cancelReminder(scheduleId)
    }

    private suspend fun handleSnoozeDose(intent: Intent, scheduleId: String, notificationId: Int) {
        val medicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: "1 dose"
        val gracePeriodMinutes = intent.getIntExtra(EXTRA_GRACE_PERIOD_MINUTES, 30)
        val repeatCount = intent.getIntExtra(EXTRA_REPEAT_COUNT, 0)

        val userSettings = userSettingsRepository.getUserSettings().firstOrNull()
        val snoozeMinutes = userSettings?.snoozeIntervalMinutes ?: DEFAULT_SNOOZE_MINUTES
        val snoozeEpoch = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

        alarmScheduler.scheduleReminder(
            scheduleId = scheduleId,
            medicineName = medicineName,
            dosage = dosage,
            doseEpochMillis = snoozeEpoch,
            gracePeriodMinutes = gracePeriodMinutes,
            repeatCount = repeatCount + 1
        )
        notificationManager.cancelNotification(notificationId)
    }
}
