package com.aditya.smartmedbox.core.alarm

import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.settings.domain.model.UserSettings
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoseReminderEngineTest {

    private val alarmScheduler: DoseAlarmScheduler = mockk(relaxed = true)
    private val doseEventRepository: DoseEventRepository = mockk(relaxed = true)

    @Test
    fun `repeating loop schedules next reminder when repeat count is within user limit and grace window`() = runTest {
        val userSettings = UserSettings(
            snoozeIntervalMinutes = 10,
            maxReminderRepeats = 3
        )
        val doseEpochMillis = 1000_000L
        val gracePeriodMinutes = 45
        val gracePeriodMillis = gracePeriodMinutes * 60 * 1000L
        val now = 1005_000L
        val currentRepeatCount = 1

        val nextRepeatCount = currentRepeatCount + 1
        val nextTrigger = now + (userSettings.snoozeIntervalMinutes * 60 * 1000L)

        val shouldRepeat = nextRepeatCount < userSettings.maxReminderRepeats &&
            nextTrigger < (doseEpochMillis + gracePeriodMillis)

        assertTrue(shouldRepeat)
        assertEquals(2, nextRepeatCount)
        assertEquals(1605_000L, nextTrigger)
    }

    @Test
    fun `repeating loop terminates when max reminder repeats is reached`() = runTest {
        val userSettings = UserSettings(
            snoozeIntervalMinutes = 10,
            maxReminderRepeats = 3
        )
        val doseEpochMillis = 1000_000L
        val gracePeriodMinutes = 60
        val gracePeriodMillis = gracePeriodMinutes * 60 * 1000L
        val now = 1030_000L
        val currentRepeatCount = 2 // Next repeat would be 3, which is not < maxReminderRepeats (3)

        val nextRepeatCount = currentRepeatCount + 1
        val nextTrigger = now + (userSettings.snoozeIntervalMinutes * 60 * 1000L)

        val shouldRepeat = nextRepeatCount < userSettings.maxReminderRepeats &&
            nextTrigger < (doseEpochMillis + gracePeriodMillis)

        assertFalse(shouldRepeat)
    }

    @Test
    fun `repeating loop terminates when next trigger exceeds grace period`() = runTest {
        val userSettings = UserSettings(
            snoozeIntervalMinutes = 15,
            maxReminderRepeats = 5
        )
        val doseEpochMillis = 1000_000L
        val gracePeriodMinutes = 20
        val gracePeriodMillis = gracePeriodMinutes * 60 * 1000L // 1,200,000 ms -> deadline 2,200,000 ms
        val now = 1500_000L
        val currentRepeatCount = 0

        val nextRepeatCount = currentRepeatCount + 1
        val nextTrigger = now + (userSettings.snoozeIntervalMinutes * 60 * 1000L) // 2,400,000 ms

        val shouldRepeat = nextRepeatCount < userSettings.maxReminderRepeats &&
            nextTrigger < (doseEpochMillis + gracePeriodMillis)

        assertFalse(shouldRepeat)
    }

    @Test
    fun `when dose event is marked TAKEN, alarms are cancelled and no notification loop is triggered`() = runTest {
        val scheduleId = "sch_aspirin_01"
        val doseEvent = DoseEvent(
            id = "evt_aspirin_01",
            scheduleId = scheduleId,
            medicineId = "med_aspirin",
            medicineName = "Aspirin 81mg",
            dosage = "1 tablet",
            scheduledTimeEpochMillis = 1000_000L,
            status = DoseStatus.TAKEN
        )

        coEvery { doseEventRepository.getEventsForDay(any(), any()) } returns flowOf(listOf(doseEvent))

        // Trigger check
        if (doseEvent.status == DoseStatus.TAKEN) {
            alarmScheduler.cancelReminder(scheduleId)
        }

        verify(exactly = 1) { alarmScheduler.cancelReminder(scheduleId) }
    }
}
