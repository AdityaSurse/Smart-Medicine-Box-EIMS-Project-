package com.aditya.smartmedbox.feature.device.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val isPairingState = MutableStateFlow(false)
    private val feedbackState = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow<DeviceUiState>(DeviceUiState.Loading)
    val uiState: StateFlow<DeviceUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DeviceUiEffect>()
    val uiEffect: SharedFlow<DeviceUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeDiscoveredDevice()
    }

    private fun observeDiscoveredDevice() {
        viewModelScope.launch {
            _uiState.value = DeviceUiState.Loading
            combine(
                deviceRepository.observeDiscoveredDevice(),
                isPairingState,
                feedbackState
            ) { device, isPairing, feedback ->
                if (device == null) {
                    DeviceUiState.Empty()
                } else {
                    DeviceUiState.Success(
                        device = device,
                        isPairingInProgress = isPairing,
                        pairingSuccessMessage = feedback
                    )
                }
            }.collect { state ->
                _uiState.value = state
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
                viewModelScope.launch {
                    isPairingState.value = true
                    feedbackState.value = "Configuring Bays..."
                    deviceRepository.pairDevice(event.deviceId)
                    isPairingState.value = false
                    feedbackState.value = "Box Connected"
                    _uiEffect.emit(DeviceUiEffect.ShowSnackbar("DoseGuard Smart Box paired successfully!"))
                    _uiEffect.emit(DeviceUiEffect.NavigateToAddMedicine)
                }
            }
            DeviceUiEvent.Troubleshoot -> {
                viewModelScope.launch {
                    _uiEffect.emit(DeviceUiEffect.ShowSnackbar("Troubleshooting guide: Verify 5V power supply and bluetooth distance."))
                }
            }
            DeviceUiEvent.ClearFeedback -> {
                feedbackState.value = null
            }
        }
    }
}
