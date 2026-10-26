package com.aditya.smartmedbox.feature.history.domain.model

enum class IntakeStatus {
    TAKEN,
    MISSED,
    ANOMALY
}

data class IntakeLog(
    val id: String,
    val scheduleId: String,
    val timestamp: Long,
    val measuredDeltaWeightGrams: Float,
    val status: IntakeStatus
)
