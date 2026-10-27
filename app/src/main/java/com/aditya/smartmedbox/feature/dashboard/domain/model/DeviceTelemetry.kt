package com.aditya.smartmedbox.feature.dashboard.domain.model

data class DeviceTelemetry(
    val deviceId: String = "DG-4081",
    val patientName: String = "Eleanor",
    val weightGrams: Float = 142.5f,
    val isBoxOpen: Boolean = false,
    val temperatureCelsius: Float = 28.4f,
    val humidityPercent: Float = 55.0f,
    val tabletsRemaining: Int = 18,
    val cadenceStatus: String = "On time",
    val isOnline: Boolean = true,
    val batteryPercent: Int = 84,
    val nextDoseMedicine: String = "Amoxicillin 500mg",
    val nextDoseDosage: String = "1 tablet",
    val nextDoseTime: String = "9:00 PM",
    val nextDoseCountdown: String = "10h 30m",
    val nextDoseTray: String = "Tray 02",
    val lastTakenTime: String = "1:30 PM",
    val cycleProgressFraction: Float = 0.68f,
    val timestamp: Long = System.currentTimeMillis()
)
