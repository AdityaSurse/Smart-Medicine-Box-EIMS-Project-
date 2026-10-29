package com.aditya.smartmedbox.feature.schedule.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import io.mockk.every
import io.mockk.mockk
import java.util.Calendar
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetTodayScheduleUseCaseTest {

    private val doseScheduleRepository: DoseScheduleRepository = mockk()
    private val doseEventRepository: DoseEventRepository = mockk()
    private val medicinesRepository: MedicinesRepository = mockk()
    private lateinit var useCase: GetTodayScheduleUseCase

    @Before
    fun setUp() {
        useCase = GetTodayScheduleUseCase(
            doseScheduleRepository,
            doseEventRepository,
            medicinesRepository
        )
    }

    @Test
    fun `invoke combines active schedules and returns today dose events`() = runTest {
        val calendar = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 28, 10, 0, 0) // Wednesday = 3
        }
        val currentTime = calendar.timeInMillis

        val schedule = DoseSchedule(
            id = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            times = listOf("08:00 AM", "08:00 PM"),
            daysOfWeek = listOf(1, 2, 3, 4, 5),
            gracePeriodMinutes = 30,
            compartmentBay = "Bay 01"
        )
        val medicine = Medicine(
            id = "med_1",
            name = "Amoxicillin",
            dosageDescription = "500 mg capsule"
        )

        every { doseScheduleRepository.getSchedules() } returns flowOf(listOf(schedule))
        every { doseEventRepository.getEventsForDay(any(), any()) } returns flowOf(emptyList())
        every { medicinesRepository.getMedicines() } returns flowOf(listOf(medicine))

        useCase(currentTime).test {
            val items = awaitItem()
            assertEquals(2, items.size)
            assertEquals("Amoxicillin", items[0].medicineName)
            assertEquals("Bay 01", items[0].bayLabel)
            awaitComplete()
        }
    }
}
