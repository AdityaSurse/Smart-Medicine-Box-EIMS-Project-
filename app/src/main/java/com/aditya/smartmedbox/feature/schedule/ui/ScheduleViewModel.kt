package com.aditya.smartmedbox.feature.schedule.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val selectedDayState = MutableStateFlow(16)
    private val feedbackMessageState = MutableStateFlow<String?>(null)
    private val isSavingState = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ScheduleUiEffect>()
    val uiEffect: SharedFlow<ScheduleUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeSchedule()
    }

    private fun observeSchedule() {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            combine(
                scheduleRepository.getScheduleDays(),
                selectedDayState,
                feedbackMessageState,
                isSavingState
            ) { days, selectedDay, feedback, isSaving ->
                val updatedDays = days.map { day ->
                    day.copy(isSelected = day.dayNumber == selectedDay)
                }
                Triple(updatedDays, selectedDay, Pair(feedback, isSaving))
            }.flatMapLatest { (days, selectedDay, feedbackAndSaving) ->
                val (feedback, isSaving) = feedbackAndSaving
                scheduleRepository.getSchedulesForDay(selectedDay).map { schedules ->
                    if (schedules.isEmpty()) {
                        ScheduleUiState.Empty(
                            days = days,
                            selectedDayNumber = selectedDay,
                            message = "No regimens scheduled for this day."
                        )
                    } else {
                        ScheduleUiState.Success(
                            days = days,
                            selectedDayNumber = selectedDay,
                            schedules = schedules,
                            isTraySynced = true,
                            isSavingSchedule = isSaving,
                            userFeedbackMessage = feedback
                        )
                    }
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.SelectDay -> {
                selectedDayState.value = event.dayNumber
            }
            is ScheduleUiEvent.ToggleActive -> {
                viewModelScope.launch {
                    scheduleRepository.toggleScheduleActive(event.scheduleId, event.isActive)
                }
            }
            is ScheduleUiEvent.SaveSchedule -> {
                saveSchedule(event)
            }
            ScheduleUiEvent.Refresh -> {
                observeSchedule()
            }
            ScheduleUiEvent.ClearFeedback -> {
                feedbackMessageState.value = null
            }
        }
    }

    private fun saveSchedule(event: ScheduleUiEvent.SaveSchedule) {
        viewModelScope.launch {
            isSavingState.value = true
            val newSchedule = PrescriptionSchedule(
                id = "sch_" + UUID.randomUUID().toString().take(8),
                medicineName = event.medicineName,
                dosageStrength = event.dosageStrength,
                formFactor = event.formFactor,
                trayBay = event.compartmentBay,
                dispenseTimes = event.dispenseTimes,
                daysOfWeek = event.repeatDays,
                status = "Dispensing",
                totalSupply = event.totalSupply,
                isActive = true
            )
            scheduleRepository.addSchedule(newSchedule)
            isSavingState.value = false
            feedbackMessageState.value = "Scheduled & Dispenser Armed"
            _uiEffect.emit(ScheduleUiEffect.ShowSnackbar("Regimen added and armed for dispenser bay"))
        }
    }
}
