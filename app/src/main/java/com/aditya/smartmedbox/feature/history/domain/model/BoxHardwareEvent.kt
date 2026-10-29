package com.aditya.smartmedbox.feature.history.domain.model

data class BoxHardwareEvent(
    val eventId: String,
    val eventType: HardwareEventType,
    val timestampEpochMillis: Long,
    val deltaWeightGrams: Float = 0.0f,
    val currentWeightGrams: Float = 0.0f,
    val trayBay: String = "Bay 01",
    val isOfflineRecorded: Boolean = false,
    val details: String = ""
)

enum class HardwareEventType {
    BOX_OPENED,
    BOX_CLOSED,
    WEIGHT_DROP,
    WEIGHT_INCREASE,
    TAMPER_ALERT,
    LOW_BATTERY,
    DOSE_DISPENSED
}

