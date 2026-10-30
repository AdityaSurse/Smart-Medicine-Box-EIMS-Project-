package com.aditya.smartmedbox.feature.device.ui

import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.model.WifiNetwork

sealed interface DeviceUiState {
    data object Loading : DeviceUiState

    data class Success(
        val device: SmartBoxDevice,
        val isPairingInProgress: Boolean = false,
        val isUnpairingInProgress: Boolean = false,
        val pairingSuccessMessage: String? = null,
        val isWifiProvisioningActive: Boolean = false,
        val availableWifiNetworks: List<WifiNetwork> = emptyList(),
        val provisioningStage: String? = null
    ) : DeviceUiState

    data class Empty(
        val message: String = "No DoseGuard smart boxes claimed yet. Scan the box QR code or enter credentials manually.",
        val manualDeviceId: String = "",
        val manualPairingToken: String = "",
        val isPairing: Boolean = false
    ) : DeviceUiState

    data class Error(val message: String) : DeviceUiState

    data class Offline(
        val message: String = "Bluetooth adapter is powered off or unauthorized."
    ) : DeviceUiState
}

sealed interface DeviceUiEvent {
    data object StartScan : DeviceUiEvent
    data class PairDevice(val deviceId: String, val pairingToken: String = "") : DeviceUiEvent
    data class UnpairDevice(val deviceId: String) : DeviceUiEvent
    data class ScanQrCode(val rawQr: String) : DeviceUiEvent
    data class UpdateManualFields(val deviceId: String, val pairingToken: String) : DeviceUiEvent
    data object StartWifiProvisioning : DeviceUiEvent
    data class SubmitWifiCredentials(val ssid: String, val passphrase: String) : DeviceUiEvent
    data object CancelWifiProvisioning : DeviceUiEvent
    data object Troubleshoot : DeviceUiEvent
    data object ClearFeedback : DeviceUiEvent
}

sealed interface DeviceUiEffect {
    data object NavigateToAddMedicine : DeviceUiEffect
    data class ShowSnackbar(val message: String) : DeviceUiEffect
    data object LaunchQrScanner : DeviceUiEffect
}
