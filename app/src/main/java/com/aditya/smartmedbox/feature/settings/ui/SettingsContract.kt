package com.aditya.smartmedbox.feature.settings.ui

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val deviceId: String = "ESP32_MED_BOX_01",
        val isNotificationsEnabled: Boolean = true
    ) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data object LoadSettings : SettingsUiEvent
}
