package com.aditya.smartmedbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
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
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val feedbackState = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsUiEffect>()
    val uiEffect: SharedFlow<SettingsUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            combine(
                settingsRepository.getSettings(),
                feedbackState
            ) { settings, feedback ->
                SettingsUiState.Success(
                    settings = settings,
                    feedbackMessage = feedback
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SetSnoozeInterval -> {
                viewModelScope.launch {
                    settingsRepository.updateSnoozeInterval(event.minutes)
                    feedbackState.value = "Snooze interval set to ${event.minutes} min"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Snooze interval updated to ${event.minutes} min"))
                }
            }
            is SettingsUiEvent.SetRefillThreshold -> {
                viewModelScope.launch {
                    settingsRepository.updateRefillThreshold(event.tablets)
                    feedbackState.value = "Refill alert threshold set to ${event.tablets} tablets"
                }
            }
            is SettingsUiEvent.ToggleTheme -> {
                viewModelScope.launch {
                    settingsRepository.updateThemeMode(event.isDark)
                    feedbackState.value = if (event.isDark) "Char dark mode enabled" else "Bone light mode enabled"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Appearance updated"))
                }
            }
            SettingsUiEvent.PlayTestChime -> {
                viewModelScope.launch {
                    settingsRepository.playTestChime()
                    feedbackState.value = "Playing Gentle Chime on Smart Box..."
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Smart Box chime triggered"))
                }
            }
            SettingsUiEvent.ClearFeedback -> {
                feedbackState.value = null
            }
            SettingsUiEvent.Refresh -> {
                observeSettings()
            }
        }
    }
}
