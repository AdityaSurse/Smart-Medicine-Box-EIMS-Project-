package com.aditya.smartmedbox.feature.device.ui

import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice

sealed interface DeviceUiState {
    data object Loading : DeviceUiState

    data class Success(
        val device: SmartBoxDevice,
        val isPairingInProgress: Boolean = false,
        val pairingSuccessMessage: String? = null
    ) : DeviceUiState

    data class Empty(
        val message: String = "No DoseGuard smart boxes discovered within Bluetooth LE range."
    ) : DeviceUiState

    data class Error(val message: String) : DeviceUiState

    data class Offline(
        val message: String = "Bluetooth adapter is powered off or unauthorized."
    ) : DeviceUiState
}

sealed interface DeviceUiEvent {
    data object StartScan : DeviceUiEvent
    data class PairDevice(val deviceId: String) : DeviceUiEvent
    data object Troubleshoot : DeviceUiEvent
    data object ClearFeedback : DeviceUiEvent
}

sealed interface DeviceUiEffect {
    data object NavigateToAddMedicine : DeviceUiEffect
    data class ShowSnackbar(val message: String) : DeviceUiEffect
}

