package com.aditya.smartmedbox.feature.device.domain.model

data class WifiNetwork(
    val ssid: String,
    val rssi: Int,
    val isSecure: Boolean = true
)

sealed interface ProvisioningState {
    data object Idle : ProvisioningState
    data object Scanning : ProvisioningState
    data class Configuring(val stageDescription: String) : ProvisioningState
    data class Success(val ipAddress: String) : ProvisioningState
    data class Error(val reason: String) : ProvisioningState
}

sealed interface ProvisioningResult {
    data class Success(val ipAddress: String, val macAddress: String) : ProvisioningResult
    data class Failure(val reason: String) : ProvisioningResult
}
