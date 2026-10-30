package com.aditya.smartmedbox.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.history.domain.repository.HistoryRepository
import com.aditya.smartmedbox.feature.history.domain.util.HistoryExporter
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val historyRepository: HistoryRepository
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
            SettingsUiEvent.SignOut -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Signed out successfully"))
                }
            }
            is SettingsUiEvent.SetUnitSystem -> {
                viewModelScope.launch {
                    settingsRepository.updateUnitSystem(event.unitSystem)
                    feedbackState.value = "Unit system set to ${event.unitSystem}"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Measurement units updated"))
                }
            }
            is SettingsUiEvent.SetGracePeriod -> {
                viewModelScope.launch {
                    settingsRepository.updateGracePeriod(event.minutes)
                    feedbackState.value = "Grace period set to ${event.minutes} min"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Dose grace period updated"))
                }
            }
            is SettingsUiEvent.SetReminderRepeat -> {
                viewModelScope.launch {
                    settingsRepository.updateReminderRepeatCount(event.count)
                    feedbackState.value = "Reminder repeat loop set to ${event.count} times"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("Reminder repeat count updated"))
                }
            }
            is SettingsUiEvent.ToggleQuietHours -> {
                viewModelScope.launch {
                    settingsRepository.updateQuietHours(event.enabled)
                    feedbackState.value = if (event.enabled) "Quiet hours activated" else "Quiet hours disabled"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar(if (event.enabled) "Quiet hours enabled" else "Quiet hours disabled"))
                }
            }
            SettingsUiEvent.ExportHistoryCsv -> {
                viewModelScope.launch {
                    val logs = historyRepository.getIntakeHistory().first()
                    val csv = HistoryExporter.exportToCsv(logs)
                    _uiEffect.emit(
                        SettingsUiEffect.ShareExport(
                            content = csv,
                            mimeType = "text/csv",
                            title = "Export Intake History (CSV)"
                        )
                    )
                }
            }
            SettingsUiEvent.ExportHistorySummary -> {
                viewModelScope.launch {
                    val logs = historyRepository.getIntakeHistory().first()
                    val summary = HistoryExporter.exportToPrintableSummary(logs)
                    _uiEffect.emit(
                        SettingsUiEffect.ShareExport(
                            content = summary,
                            mimeType = "text/plain",
                            title = "Export Clinical Adherence Report"
                        )
                    )
                }
            }
            SettingsUiEvent.ClearAllData -> {
                viewModelScope.launch {
                    settingsRepository.clearAllData()
                    feedbackState.value = "All local health and scheduling data wiped"
                    _uiEffect.emit(SettingsUiEffect.ShowSnackbar("All medical data successfully deleted"))
                }
            }
        }
    }
}
