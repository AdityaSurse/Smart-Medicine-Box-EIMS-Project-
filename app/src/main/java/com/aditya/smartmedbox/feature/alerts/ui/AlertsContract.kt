package com.aditya.smartmedbox.feature.alerts.ui

sealed interface AlertsUiState {
    data object Loading : AlertsUiState

    data class Success(val alerts: List<String> = emptyList()) : AlertsUiState
}

sealed interface AlertsUiEvent {
    data object LoadAlerts : AlertsUiEvent
}
