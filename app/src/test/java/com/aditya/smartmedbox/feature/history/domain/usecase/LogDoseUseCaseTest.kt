package com.aditya.smartmedbox.feature.history.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogDoseUseCaseTest {

    private val doseEventRepository: DoseEventRepository = mockk()
    private val medicinesRepository: MedicinesRepository = mockk()
    private lateinit var useCase: LogDoseUseCase

    @Before
    fun setUp() {
        useCase = LogDoseUseCase(doseEventRepository, medicinesRepository)
    }

    @Test
    fun `invoke with TAKEN status decrements medicine stock`() = runTest {
        val event = DoseEvent(
            id = "evt_01",
            scheduleId = "sch_01",
            medicineId = "med_amox",
            medicineName = "Amoxicillin",
            dosage = "500 mg",
            scheduledTimeEpochMillis = 1700000000L
        )
        val medicine = Medicine(
            id = "med_amox",
            name = "Amoxicillin",
            dosageDescription = "500 mg",
            currentStock = 20
        )

        coEvery {
            doseEventRepository.logDose("evt_01", DoseStatus.TAKEN, any(), any())
        } returns Result.success(Unit)
        every { doseEventRepository.getDoseEventById("evt_01") } returns flowOf(event)
        every { medicinesRepository.getMedicineById("med_amox") } returns flowOf(medicine)
        coEvery { medicinesRepository.updateStock("med_amox", 19) } returns Result.success(Unit)

        val result = useCase("evt_01", DoseStatus.TAKEN, 1700000050L, 0.55f)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { medicinesRepository.updateStock("med_amox", 19) }
    }

    @Test
    fun `invoke with MISSED status does not decrement medicine stock`() = runTest {
        coEvery {
            doseEventRepository.logDose("evt_02", DoseStatus.MISSED, any(), any())
        } returns Result.success(Unit)

        val result = useCase("evt_02", DoseStatus.MISSED, 1700003600L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { medicinesRepository.updateStock(any(), any()) }
    }
}
