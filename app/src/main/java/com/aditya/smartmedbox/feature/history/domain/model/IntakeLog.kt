package com.aditya.smartmedbox.feature.history.domain.model

import com.aditya.smartmedbox.core.designsystem.component.DoseStatus

enum class IntakeStatus {
    TAKEN,
    PENDING,
    MISSED,
    ANOMALY
}

data class IntakeLog(
    val id: String,
    val medicineName: String,
    val dosageInstructions: String,
    val timeAndBay: String,
    val dateGroup: String,
    val timestamp: Long,
    val measuredDeltaWeightGrams: Float = 0.5f,
    val status: DoseStatus
)

data class DailyAdherence(
    val dayLabel: String, // "M", "T", "W", "T", "F", "S", "S"
    val takenCount: Int,
    val missedCount: Int
)

data class AdherenceStats(
    val overallAdherenceRatePercent: Int = 92,
    val streakDays: Int = 14,
    val streakSubtitle: String = "Morning doses uninterrupted",
    val chamberTemp: String = "28.4°C",
    val chamberHumidity: String = "55%",
    val weeklyBreakdown: List<DailyAdherence> = listOf(
        DailyAdherence("M", 4, 0),
        DailyAdherence("T", 4, 0),
        DailyAdherence("W", 3, 1),
        DailyAdherence("T", 4, 0),
        DailyAdherence("F", 4, 0),
        DailyAdherence("S", 3, 1),
        DailyAdherence("S", 4, 0)
    ),
    val totalTaken: Int = 26,
    val totalMissed: Int = 2
)
