package com.aditya.smartmedbox.feature.dashboard.ui

data class DeviceTelemetryUi(
    val currentWeightGrams: Float = 0f,
    val isBoxOpen: Boolean = false,
    val temperatureCelsius: Float = 24.5f,
    val humidityPercent: Float = 45.0f,
    val isOnline: Boolean = true
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val deviceTelemetry: DeviceTelemetryUi = DeviceTelemetryUi(),
        val nextScheduledDose: String = "10:00 AM",
        val nextMedicationName: String = "Amoxicillin 500mg"
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}

sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent

    data object OpenDispenserCommand : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data class ShowSnackbar(val message: String) : DashboardUiEffect
}
