package com.aditya.smartmedbox.feature.settings.domain.model

data class DeviceSettings(
    val deviceId: String,
    val wifiSsid: String,
    val isBuzzerEnabled: Boolean,
    val tareOffsetGrams: Float,
    val scaleFactor: Float
)
