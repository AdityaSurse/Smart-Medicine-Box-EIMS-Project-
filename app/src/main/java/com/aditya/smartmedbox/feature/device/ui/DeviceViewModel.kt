package com.aditya.smartmedbox.feature.device.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.device.domain.model.ProvisioningState
import com.aditya.smartmedbox.feature.device.domain.model.WifiNetwork
import com.aditya.smartmedbox.feature.device.domain.provisioning.WifiProvisioner
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import com.aditya.smartmedbox.feature.device.domain.usecase.ObserveDeviceUseCase
import com.aditya.smartmedbox.feature.device.domain.usecase.PairDeviceUseCase
import com.aditya.smartmedbox.feature.device.domain.usecase.UnpairDeviceUseCase
import com.aditya.smartmedbox.feature.device.domain.util.QrPairingParser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val pairDeviceUseCase: PairDeviceUseCase,
    private val unpairDeviceUseCase: UnpairDeviceUseCase,
    private val observeDeviceUseCase: ObserveDeviceUseCase,
    private val wifiProvisioner: WifiProvisioner
) : ViewModel() {

    private val isPairingState = MutableStateFlow(false)
    private val isUnpairingState = MutableStateFlow(false)
    private val feedbackState = MutableStateFlow<String?>(null)
    private val manualDeviceId = MutableStateFlow("")
    private val manualPairingToken = MutableStateFlow("")

    private val isWifiProvisioningActive = MutableStateFlow(false)
    private val availableWifiNetworks = MutableStateFlow<List<WifiNetwork>>(emptyList())
    private val provisioningStage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DeviceUiEffect>()
    val uiEffect: SharedFlow<DeviceUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeDiscoveredDevice()
        observeProvisioning()
    }

    private fun observeDiscoveredDevice() {
        viewModelScope.launch {
            _uiState.value = DeviceUiState.Loading
            combine(
                observeDeviceUseCase(),
                isPairingState,
                isUnpairingState,
                feedbackState,
                combine(
                    manualDeviceId,
                    manualPairingToken,
                    isWifiProvisioningActive,
                    availableWifiNetworks,
                    provisioningStage
                ) { devId, token, isProv, networks, stage ->
                    ProvisioningBundle(devId, token, isProv, networks, stage)
                }
            ) { device, isPairing, isUnpairing, feedback, bundle ->
                if (device == null) {
                    DeviceUiState.Empty(
                        manualDeviceId = bundle.manualDeviceId,
                        manualPairingToken = bundle.manualPairingToken,
                        isPairing = isPairing
                    )
                } else {
                    DeviceUiState.Success(
                        device = device,
                        isPairingInProgress = isPairing,
                        isUnpairingInProgress = isUnpairing,
                        pairingSuccessMessage = feedback,
                        isWifiProvisioningActive = bundle.isWifiProvisioningActive,
                        availableWifiNetworks = bundle.availableWifiNetworks,
                        provisioningStage = bundle.provisioningStage
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun observeProvisioning() {
        viewModelScope.launch {
            wifiProvisioner.observeProvisioningState().collect { state ->
                when (state) {
                    is ProvisioningState.Configuring -> provisioningStage.value = state.stageDescription
                    is ProvisioningState.Success -> {
                        provisioningStage.value = "Connected! IP: ${state.ipAddress}"
                        _uiEffect.emit(DeviceUiEffect.ShowSnackbar("Smart Box successfully provisioned on Wi-Fi"))
                    }
                    is ProvisioningState.Error -> {
                        provisioningStage.value = null
                        _uiEffect.emit(DeviceUiEffect.ShowSnackbar("Provisioning Error: ${state.reason}"))
                    }
                    else -> Unit
                }
            }
        }
    }

    fun onEvent(event: DeviceUiEvent) {
        when (event) {
            DeviceUiEvent.StartScan -> {
                viewModelScope.launch {
                    _uiState.value = DeviceUiState.Loading
                    deviceRepository.startScan()
                    observeDiscoveredDevice()
                }
            }
            is DeviceUiEvent.PairDevice -> {
                executePairing(event.deviceId, event.pairingToken)
            }
            is DeviceUiEvent.UnpairDevice -> {
                viewModelScope.launch {
                    isUnpairingState.value = true
                    val result = unpairDeviceUseCase(event.deviceId)
                    isUnpairingState.value = false
                    if (result.isSuccess) {
                        _uiEffect.emit(DeviceUiEffect.ShowSnackbar("Smart Box unpaired from account"))
                    } else {
                        _uiEffect.emit(
                            DeviceUiEffect.ShowSnackbar(
                                result.exceptionOrNull()?.message ?: "Failed to unpair box"
                            )
                        )
                    }
                }
            }
            is DeviceUiEvent.ScanQrCode -> {
                val parsed = QrPairingParser.parse(event.rawQr)
                executePairing(parsed.deviceId, parsed.pairingToken)
            }
            is DeviceUiEvent.UpdateManualFields -> {
                manualDeviceId.value = event.deviceId
                manualPairingToken.value = event.pairingToken
            }
            DeviceUiEvent.StartWifiProvisioning -> {
                viewModelScope.launch {
                    isWifiProvisioningActive.value = true
                    provisioningStage.value = "Scanning local 2.4 GHz Wi-Fi..."
                    val scanResult = wifiProvisioner.scanAvailableNetworks()
                    if (scanResult.isSuccess) {
                        availableWifiNetworks.value = scanResult.getOrDefault(emptyList())
                        provisioningStage.value = null
                    } else {
                        provisioningStage.value = "Wi-Fi scan failed"
                    }
                }
            }
            is DeviceUiEvent.SubmitWifiCredentials -> {
                viewModelScope.launch {
                    val currentDevice = (_uiState.value as? DeviceUiState.Success)?.device
                    val deviceId = currentDevice?.deviceId ?: "DG-4081"
                    val token = currentDevice?.pairingCode ?: "8820"
                    val result = wifiProvisioner.provisionDevice(
                        deviceId = deviceId,
                        pairingToken = token,
                        ssid = event.ssid,
                        passphrase = event.passphrase
                    )
                    if (result.isSuccess) {
                        isWifiProvisioningActive.value = false
                    }
                }
            }
            DeviceUiEvent.CancelWifiProvisioning -> {
                isWifiProvisioningActive.value = false
                provisioningStage.value = null
            }
            DeviceUiEvent.Troubleshoot -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        DeviceUiEffect.ShowSnackbar(
                            "Troubleshooting guide: Verify 5V power supply and bluetooth distance."
                        )
                    )
                }
            }
            DeviceUiEvent.ClearFeedback -> {
                feedbackState.value = null
            }
        }
    }

    private fun executePairing(deviceId: String, pairingToken: String) {
        viewModelScope.launch {
            isPairingState.value = true
            feedbackState.value = "Claiming Smart Box in Cloud..."
            val result = pairDeviceUseCase(deviceId, pairingToken)
            isPairingState.value = false
            if (result.isSuccess) {
                feedbackState.value = "Box Connected"
                _uiEffect.emit(DeviceUiEffect.ShowSnackbar("DoseGuard Smart Box paired successfully!"))
                _uiEffect.emit(DeviceUiEffect.NavigateToAddMedicine)
            } else {
                feedbackState.value = null
                _uiEffect.emit(
                    DeviceUiEffect.ShowSnackbar(
                        result.exceptionOrNull()?.message ?: "Device pairing failed"
                    )
                )
            }
        }
    }

    private data class ProvisioningBundle(
        val manualDeviceId: String,
        val manualPairingToken: String,
        val isWifiProvisioningActive: Boolean,
        val availableWifiNetworks: List<WifiNetwork>,
        val provisioningStage: String?
    )
}
