package com.aditya.smartmedbox.feature.alerts.ui

import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert

sealed interface AlertsUiState {
    data object Loading : AlertsUiState

    data class Success(
        val alerts: List<AdherenceAlert>,
        val activeToastMessage: String? = null,
        val isTakenRecorded: Boolean = false
    ) : AlertsUiState

    data class Empty(
        val message: String = "No urgent alerts. All medication intakes are on schedule."
    ) : AlertsUiState

    data class Error(val message: String) : AlertsUiState

    data class Offline(
        val alerts: List<AdherenceAlert>,
        val message: String = "Box offline. Showing cached alert state."
    ) : AlertsUiState
}

sealed interface AlertsUiEvent {
    data class MarkTaken(val alertId: String) : AlertsUiEvent
    data class Snooze(val alertId: String, val minutes: Int = 10) : AlertsUiEvent
    data class SkipDose(val alertId: String, val reason: String? = null) : AlertsUiEvent
    data object ClearToast : AlertsUiEvent
    data object Refresh : AlertsUiEvent
}

sealed interface AlertsUiEffect {
    data class ShowSnackbar(val message: String) : AlertsUiEffect
    data object NavigateBack : AlertsUiEffect
}
