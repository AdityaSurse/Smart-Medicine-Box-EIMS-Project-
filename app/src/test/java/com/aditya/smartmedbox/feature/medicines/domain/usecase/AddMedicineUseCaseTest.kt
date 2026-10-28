package com.aditya.smartmedbox.feature.medicines.domain.usecase

import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddMedicineUseCaseTest {

    private val medicinesRepository: MedicinesRepository = mockk()
    private val doseScheduleRepository: DoseScheduleRepository = mockk()
    private lateinit var useCase: AddMedicineUseCase

    @Before
    fun setUp() {
        useCase = AddMedicineUseCase(medicinesRepository, doseScheduleRepository)
    }

    @Test
    fun `invoke with valid medicine saves medicine and creates dose schedule`() = runTest {
        val medicine = Medicine(
            id = "med_amox",
            name = "Amoxicillin",
            dosageDescription = "500 mg capsule",
            strengthMg = 500,
            compartmentLabel = "Bay 01",
            timings = listOf("08:00 AM", "08:00 PM"),
            repeatDays = listOf("M", "T", "W"),
            currentStock = 20
        )

        coEvery { medicinesRepository.saveMedicine(any()) } returns Result.success(Unit)
        coEvery { doseScheduleRepository.saveSchedule(any()) } returns Result.success(Unit)

        val result = useCase(medicine)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { medicinesRepository.saveMedicine(medicine) }
        coVerify(exactly = 1) {
            doseScheduleRepository.saveSchedule(
                match { it.medicineId == "med_amox" && it.compartmentBay == "Bay 01" }
            )
        }
    }

    @Test
    fun `invoke with blank name returns failure`() = runTest {
        val medicine = Medicine(
            id = "med_invalid",
            name = "   ",
            dosageDescription = "1 tablet",
            currentStock = 10
        )

        val result = useCase(medicine)

        assertTrue(result.isFailure)
        assertEquals("Medicine name cannot be blank.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke with negative stock returns failure`() = runTest {
        val medicine = Medicine(
            id = "med_invalid",
            name = "Atorvastatin",
            dosageDescription = "20 mg",
            currentStock = -5
        )

        val result = useCase(medicine)

        assertTrue(result.isFailure)
        assertEquals("Stock cannot be negative.", result.exceptionOrNull()?.message)
    }
}

