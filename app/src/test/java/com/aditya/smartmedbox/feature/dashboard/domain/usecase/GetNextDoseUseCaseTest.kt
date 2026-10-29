package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GetNextDoseUseCaseTest {

    private val getTodayScheduleUseCase: GetTodayScheduleUseCase = mockk()
    private lateinit var useCase: GetNextDoseUseCase

    @Before
    fun setUp() {
        useCase = GetNextDoseUseCase(getTodayScheduleUseCase)
    }

    @Test
    fun `invoke returns earliest upcoming pending dose`() = runTest {
        val now = 1700000000000L
        val eventMorning = DoseEvent(
            id = "evt_1",
            scheduleId = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = now - 3600000L,
            status = DoseStatus.TAKEN
        )
        val eventAfternoon = DoseEvent(
            id = "evt_2",
            scheduleId = "sch_2",
            medicineId = "med_2",
            medicineName = "Metformin",
            dosage = "850 mg",
            scheduledTimeEpochMillis = now + 1800000L,
            status = DoseStatus.PENDING
        )
        val eventEvening = DoseEvent(
            id = "evt_3",
            scheduleId = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = now + 14400000L,
            status = DoseStatus.PENDING
        )

        every { getTodayScheduleUseCase(now) } returns flowOf(
            listOf(eventMorning, eventAfternoon, eventEvening)
        )

        useCase(now).test {
            val nextDose = awaitItem()
            assertNotNull(nextDose)
            assertEquals("Metformin", nextDose?.medicineName)
            assertEquals("evt_2", nextDose?.id)
            awaitComplete()
        }
    }
}
