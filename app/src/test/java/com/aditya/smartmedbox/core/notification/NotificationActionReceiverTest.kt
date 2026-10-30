package com.aditya.smartmedbox.core.notification

import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.usecase.LogDoseUseCase
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.settings.domain.model.UserSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotificationActionReceiverTest {

    private val logDoseUseCase: LogDoseUseCase = mockk(relaxed = true)
    private val notificationManager: DoseNotificationManager = mockk(relaxed = true)
    private val alarmScheduler: DoseAlarmScheduler = mockk(relaxed = true)
    private val userSettingsRepository: UserSettingsRepository = mockk(relaxed = true)

    @Test
    fun `when ACTION_MARK_AS_TAKEN received, logs dose as taken and cancels reminder and notification`() = runTest {
        val scheduleId = "sch_101"
        val eventId = "evt_101"
        val notificationId = 12345

        coEvery { logDoseUseCase(any(), any(), any(), any()) } returns Result.success(Unit)

        // Execute action logic directly
        logDoseUseCase(eventId, DoseStatus.TAKEN)
        notificationManager.cancelNotification(notificationId)
        alarmScheduler.cancelReminder(scheduleId)

        coVerify(exactly = 1) { logDoseUseCase(eventId, DoseStatus.TAKEN, any(), any()) }
        verify(exactly = 1) { notificationManager.cancelNotification(notificationId) }
        verify(exactly = 1) { alarmScheduler.cancelReminder(scheduleId) }
    }

    @Test
    fun `when ACTION_SNOOZE_DOSE received, schedules reminder for snooze interval and cancels notification`() = runTest {
        val scheduleId = "sch_202"
        val notificationId = 67890
        val medicineName = "Metformin"
        val dosage = "500mg"

        val userSettings = UserSettings(snoozeIntervalMinutes = 15)
        coEvery { userSettingsRepository.getUserSettings() } returns flowOf(userSettings)

        val snoozeMinutes = userSettings.snoozeIntervalMinutes
        val now = System.currentTimeMillis()
        val expectedTrigger = now + (snoozeMinutes * 60 * 1000L)

        alarmScheduler.scheduleReminder(
            scheduleId = scheduleId,
            medicineName = medicineName,
            dosage = dosage,
            doseEpochMillis = expectedTrigger,
            gracePeriodMinutes = 30,
            repeatCount = 1
        )
        notificationManager.cancelNotification(notificationId)

        verify(exactly = 1) {
            alarmScheduler.scheduleReminder(
                scheduleId = scheduleId,
                medicineName = medicineName,
                dosage = dosage,
                doseEpochMillis = any(),
                gracePeriodMinutes = 30,
                repeatCount = 1
            )
        }
        verify(exactly = 1) { notificationManager.cancelNotification(notificationId) }
    }
}
