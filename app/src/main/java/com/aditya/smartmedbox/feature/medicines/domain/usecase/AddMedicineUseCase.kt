package com.aditya.smartmedbox.feature.medicines.domain.usecase

import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import com.aditya.smartmedbox.feature.schedule.domain.model.DoseSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import javax.inject.Inject

class AddMedicineUseCase @Inject constructor(
    private val medicinesRepository: MedicinesRepository,
    private val doseScheduleRepository: DoseScheduleRepository
) {
    suspend operator fun invoke(
        medicine: Medicine,
        gracePeriodMinutes: Int = 30
    ): Result<Unit> {
        if (medicine.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Medicine name cannot be blank."))
        }
        if (medicine.currentStock < 0) {
            return Result.failure(IllegalArgumentException("Stock cannot be negative."))
        }

        val saveMedResult = medicinesRepository.saveMedicine(medicine)
        if (saveMedResult.isFailure) return saveMedResult

        val schedule = DoseSchedule(
            id = "sch_${medicine.id}",
            medicineId = medicine.id,
            medicineName = medicine.name,
            times = medicine.timings,
            daysOfWeek = mapRepeatDaysToDayNumbers(medicine.repeatDays),
            gracePeriodMinutes = gracePeriodMinutes,
            compartmentBay = medicine.compartmentLabel,
            isActive = medicine.isActive
        )
        return doseScheduleRepository.saveSchedule(schedule)
    }

    private fun mapRepeatDaysToDayNumbers(days: List<String>): List<Int> {
        if (days.isEmpty()) return listOf(1, 2, 3, 4, 5, 6, 7)
        val result = mutableListOf<Int>()
        if (days.contains("M")) result.add(1)
        if (days.contains("T")) result.add(2)
        if (days.contains("W")) result.add(3)
        if (days.contains("Th") || days.count { it == "T" } > 1) result.add(4)
        if (days.contains("F")) result.add(5)
        if (days.contains("S") || days.contains("Sa")) result.add(6)
        if (days.contains("Su") || days.count { it == "S" } > 1) result.add(7)
        return if (result.isEmpty()) listOf(1, 2, 3, 4, 5) else result
    }
}

