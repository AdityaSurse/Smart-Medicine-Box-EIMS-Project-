package com.aditya.smartmedbox.feature.alerts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.alerts.domain.repository.AlertsRepository
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
class AlertsViewModel @Inject constructor(
    private val alertsRepository: AlertsRepository
) : ViewModel() {

    private val toastMessageState = MutableStateFlow<String?>(null)
    private val isTakenState = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AlertsUiEffect>()
    val uiEffect: SharedFlow<AlertsUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeAlerts()
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            _uiState.value = AlertsUiState.Loading
            combine(
                alertsRepository.getActiveAlerts(),
                toastMessageState,
                isTakenState
            ) { alerts, toast, isTaken ->
                if (alerts.isEmpty()) {
                    AlertsUiState.Empty()
                } else {
                    AlertsUiState.Success(
                        alerts = alerts,
                        activeToastMessage = toast,
                        isTakenRecorded = isTaken
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: AlertsUiEvent) {
        when (event) {
            is AlertsUiEvent.MarkTaken -> {
                viewModelScope.launch {
                    alertsRepository.markDoseTaken(event.alertId)
                    isTakenState.value = true
                    toastMessageState.value = "Dose registered as taken. Smart Box Tray 03 verified."
                    _uiEffect.emit(AlertsUiEffect.ShowSnackbar("Dose recorded as taken"))
                }
            }
            is AlertsUiEvent.Snooze -> {
                viewModelScope.launch {
                    alertsRepository.snoozeAlert(event.alertId, event.minutes)
                    toastMessageState.value = "Reminder scheduled for +${event.minutes} min. Box chime silenced."
                    _uiEffect.emit(AlertsUiEffect.ShowSnackbar("Alert snoozed for ${event.minutes} minutes"))
                }
            }
            is AlertsUiEvent.SkipDose -> {
                viewModelScope.launch {
                    alertsRepository.skipDose(event.alertId, event.reason)
                    toastMessageState.value = "Caregiver notification opened for reason log."
                    _uiEffect.emit(AlertsUiEffect.ShowSnackbar("Dose skipped. Caregiver notified."))
                }
            }
            AlertsUiEvent.ClearToast -> {
                toastMessageState.value = null
            }
            AlertsUiEvent.Refresh -> {
                observeAlerts()
            }
        }
    }
}

