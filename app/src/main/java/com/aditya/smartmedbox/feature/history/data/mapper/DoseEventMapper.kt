package com.aditya.smartmedbox.feature.history.data.mapper

import com.aditya.smartmedbox.core.database.entity.DoseEventEntity
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent

fun DoseEventEntity.toDomain(): DoseEvent {
    return DoseEvent(
        id = id,
        scheduleId = scheduleId,
        medicineId = medicineId,
        medicineName = medicineName,
        dosage = dosage,
        scheduledTimeEpochMillis = scheduledTimeEpochMillis,
        status = status,
        loggedTimeEpochMillis = loggedTimeEpochMillis,
        recordedWeightGrams = recordedWeightGrams,
        bayLabel = bayLabel
    )
}

fun DoseEvent.toEntity(): DoseEventEntity {
    return DoseEventEntity(
        id = id,
        scheduleId = scheduleId,
        medicineId = medicineId,
        medicineName = medicineName,
        dosage = dosage,
        scheduledTimeEpochMillis = scheduledTimeEpochMillis,
        status = status,
        loggedTimeEpochMillis = loggedTimeEpochMillis,
        recordedWeightGrams = recordedWeightGrams,
        bayLabel = bayLabel
    )
}

