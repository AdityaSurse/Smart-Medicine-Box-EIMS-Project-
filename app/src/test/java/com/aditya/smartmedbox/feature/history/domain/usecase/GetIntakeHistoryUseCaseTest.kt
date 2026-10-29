package com.aditya.smartmedbox.feature.history.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetIntakeHistoryUseCaseTest {

    private val doseEventRepository: DoseEventRepository = mockk()
    private lateinit var useCase: GetIntakeHistoryUseCase

    @Before
    fun setUp() {
        useCase = GetIntakeHistoryUseCase(doseEventRepository)
    }

    @Test
    fun `invoke computes adherence percentage and sorts by reverse timestamp`() = runTest {
        val event1 = DoseEvent(
            id = "e1",
            scheduleId = "s1",
            medicineId = "m1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = 1000L,
            status = DoseStatus.TAKEN
        )
        val event2 = DoseEvent(
            id = "e2",
            scheduleId = "s1",
            medicineId = "m1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = 2000L,
            status = DoseStatus.TAKEN
        )
        val event3 = DoseEvent(
            id = "e3",
            scheduleId = "s1",
            medicineId = "m1",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = 3000L,
            status = DoseStatus.MISSED
        )

        every { doseEventRepository.getDoseEvents() } returns flowOf(listOf(event1, event2, event3))

        useCase().test {
            val summary = awaitItem()
            assertEquals(3, summary.totalLogged)
            assertEquals(2, summary.takenCount)
            assertEquals(1, summary.missedCount)
            assertEquals(66, summary.adherencePercentage)
            assertEquals("e3", summary.events[0].id) // most recent first
            awaitComplete()
        }
    }
}
