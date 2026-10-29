package com.aditya.smartmedbox.feature.history.domain.model

enum class DoseStatus {
    TAKEN,
    PENDING,
    MISSED
}

data class DoseEvent(
    val id: String,
    val scheduleId: String,
    val medicineId: String,
    val medicineName: String,
    val dosage: String,
    val scheduledTimeEpochMillis: Long,
    val status: DoseStatus = DoseStatus.PENDING,
    val loggedTimeEpochMillis: Long? = null,
    val recordedWeightGrams: Float? = null,
    val bayLabel: String = "Bay 01"
)
