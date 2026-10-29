package com.aditya.smartmedbox.feature.history.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class LogDoseUseCase @Inject constructor(
    private val doseEventRepository: DoseEventRepository,
    private val medicinesRepository: MedicinesRepository
) {
    suspend operator fun invoke(eventId: String, status: DoseStatus, loggedTimestamp: Long = System.currentTimeMillis(), recordedWeightGrams: Float? = null): Result<Unit> {
        val logResult = doseEventRepository.logDose(
            eventId = eventId,
            status = status,
            loggedTimestamp = loggedTimestamp,
            recordedWeightGrams = recordedWeightGrams
        )
        if (logResult.isFailure) return logResult

        if (status == DoseStatus.TAKEN) {
            val event = doseEventRepository.getDoseEventById(eventId).firstOrNull()
            if (event != null) {
                val medicine = medicinesRepository.getMedicineById(event.medicineId).firstOrNull()
                if (medicine != null && medicine.currentStock > 0) {
                    medicinesRepository.updateStock(medicine.id, medicine.currentStock - 1)
                }
            }
        }
        return Result.success(Unit)
    }
}
