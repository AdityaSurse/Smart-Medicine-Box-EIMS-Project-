package com.aditya.smartmedbox.feature.dashboard.domain.model

data class DeviceTelemetry(
    val deviceId: String,
    val weightGrams: Float,
    val isBoxOpen: Boolean,
    val temperatureCelsius: Float,
    val humidityPercent: Float,
    val timestamp: Long
)
