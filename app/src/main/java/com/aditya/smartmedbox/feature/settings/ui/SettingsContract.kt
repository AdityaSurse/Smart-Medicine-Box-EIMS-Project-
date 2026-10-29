package com.aditya.smartmedbox.feature.settings.ui

import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val settings: DeviceSettings,
        val feedbackMessage: String? = null
    ) : SettingsUiState

    data class Empty(
        val message: String = "No paired apothecary devices found in settings."
    ) : SettingsUiState

    data class Error(val message: String) : SettingsUiState

    data class Offline(
        val settings: DeviceSettings,
        val message: String = "Box offline. Settings will synchronize on reconnect."
    ) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class SetSnoozeInterval(val minutes: Int) : SettingsUiEvent
    data class SetRefillThreshold(val tablets: Int) : SettingsUiEvent
    data class ToggleTheme(val isDark: Boolean) : SettingsUiEvent
    data object PlayTestChime : SettingsUiEvent
    data object ClearFeedback : SettingsUiEvent
    data object Refresh : SettingsUiEvent
    data object SignOut : SettingsUiEvent
}

sealed interface SettingsUiEffect {
    data class ShowSnackbar(val message: String) : SettingsUiEffect
    data object NavigateToPairDevice : SettingsUiEffect
}
