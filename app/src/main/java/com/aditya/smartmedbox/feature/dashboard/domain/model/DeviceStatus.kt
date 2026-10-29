package com.aditya.smartmedbox.feature.dashboard.domain.model

data class DeviceStatus(
    val deviceId: String = "DG_BOX_4081",
    val temperatureCelsius: Float = 21.4f,
    val humidityPercent: Float = 46.0f,
    val weightGrams: Float = 14.8f,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isBoxOpen: Boolean = false,
    val batteryPercent: Int = 84,
    val isOnline: Boolean = true
)
