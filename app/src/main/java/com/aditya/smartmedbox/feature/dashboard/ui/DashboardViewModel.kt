package com.aditya.smartmedbox.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.util.DispatcherProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel
@Inject
constructor(
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<DashboardUiState>(
            DashboardUiState.Success(
                deviceTelemetry =
                DeviceTelemetryUi(
                    currentWeightGrams = 142.5f,
                    isBoxOpen = false,
                    temperatureCelsius = 23.8f,
                    humidityPercent = 46.2f,
                    isOnline = true
                )
            )
        )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<DashboardUiEffect>()
    val effect: SharedFlow<DashboardUiEffect> = _effect.asSharedFlow()

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.Refresh -> refreshTelemetry()
            is DashboardUiEvent.OpenDispenserCommand -> sendOpenCommand()
        }
    }

    private fun refreshTelemetry() {
        viewModelScope.launch(dispatchers.io) {
            _effect.emit(DashboardUiEffect.ShowSnackbar("Telemetry refreshed"))
        }
    }

    private fun sendOpenCommand() {
        viewModelScope.launch(dispatchers.io) {
            _effect.emit(DashboardUiEffect.ShowSnackbar("Dispenser command sent to IoT box"))
        }
    }
}
