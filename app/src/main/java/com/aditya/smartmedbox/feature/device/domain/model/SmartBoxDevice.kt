package com.aditya.smartmedbox.feature.device.domain.model

data class SmartBoxDevice(
    val deviceId: String,
    val serialNumber: String = "#DG-4081",
    val modelName: String = "DoseGuard Box #DG-4081",
    val connectionType: String = "Bluetooth LE • Smart Dispenser 7-Bay",
    val signalStrength: String = "Strong Signal",
    val firmwareVersion: String = "v3.2",
    val pairingCode: String = "8820",
    val isNearby: Boolean = true,
    val isPaired: Boolean = true
)
