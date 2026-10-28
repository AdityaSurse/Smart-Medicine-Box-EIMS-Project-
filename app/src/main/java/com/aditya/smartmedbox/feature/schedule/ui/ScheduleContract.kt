package com.aditya.smartmedbox.feature.schedule.ui

import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay

sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState

    data class Success(
        val days: List<ScheduleDay> = emptyList(),
        val selectedDayNumber: Int = 16,
        val schedules: List<PrescriptionSchedule> = emptyList(),
        val isTraySynced: Boolean = true,
        val isSavingSchedule: Boolean = false,
        val userFeedbackMessage: String? = null
    ) : ScheduleUiState

    data class Empty(
        val days: List<ScheduleDay> = emptyList(),
        val selectedDayNumber: Int = 16,
        val message: String = "No regimens scheduled for this day."
    ) : ScheduleUiState

    data class Error(val message: String) : ScheduleUiState

    data class Offline(
        val days: List<ScheduleDay> = emptyList(),
        val schedules: List<PrescriptionSchedule> = emptyList(),
        val message: String = "Smart box offline. Cached apothecary schedules displayed."
    ) : ScheduleUiState
}

sealed interface ScheduleUiEvent {
    data class SelectDay(val dayNumber: Int) : ScheduleUiEvent
    data class ToggleActive(val scheduleId: String, val isActive: Boolean) : ScheduleUiEvent
    data class SaveSchedule(
        val medicineName: String,
        val dosageStrength: String,
        val compartmentBay: String,
        val dispenseTimes: List<String>,
        val repeatDays: List<String>,
        val totalSupply: Int,
        val formFactor: String
    ) : ScheduleUiEvent
    data object Refresh : ScheduleUiEvent
    data object ClearFeedback : ScheduleUiEvent
}

sealed interface ScheduleUiEffect {
    data class ShowSnackbar(val message: String) : ScheduleUiEffect
}
