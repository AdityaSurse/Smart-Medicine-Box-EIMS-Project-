package com.aditya.smartmedbox.feature.dashboard.ui

import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry

data class DashboardScheduleItem(
    val id: String,
    val time: String,
    val tray: String,
    val medicineName: String,
    val dosage: String,
    val status: DoseStatus
)

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Offline : DashboardUiState
    data class Error(val message: String) : DashboardUiState

    data class Success(
        val telemetry: DeviceTelemetry = DeviceTelemetry(),
        val isRefreshing: Boolean = false,
        val scheduleLedger: List<DashboardScheduleItem> = emptyList(),
        val caregiverNote: String = "Dr. Aris reduced Evening Steroid by 5mg.",
        val caregiverNoteAuthor: String = "Updated by Sarah • 3 hours ago",
        val weeklyAdherencePercent: Int = 94,
        val missedThisWeekCount: Int = 1,
        val isExactAlarmPermissionRequired: Boolean = false
    ) : DashboardUiState
}

sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent
    data class DispenseNow(val tray: String) : DashboardUiEvent
    data object ClearError : DashboardUiEvent
    data object RequestExactAlarmPermission : DashboardUiEvent
    data object DismissExactAlarmBanner : DashboardUiEvent
}

sealed interface DashboardUiEffect {
    data class ShowSnackbar(val message: String) : DashboardUiEffect
    data object NavigateToSchedule : DashboardUiEffect
    data object NavigateToHistory : DashboardUiEffect
    data object OpenExactAlarmSettings : DashboardUiEffect
}
