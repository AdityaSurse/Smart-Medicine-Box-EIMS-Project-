package com.aditya.smartmedbox.feature.schedule.ui

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState

    data class Success(val schedules: List<String> = emptyList()) : ScheduleUiState
}

sealed interface ScheduleUiEvent {
    data object LoadSchedules : ScheduleUiEvent
}
