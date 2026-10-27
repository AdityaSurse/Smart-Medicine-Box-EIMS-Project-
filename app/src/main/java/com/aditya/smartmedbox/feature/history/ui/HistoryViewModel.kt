package com.aditya.smartmedbox.feature.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.history.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(HistoryFilter.ALL)
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        historyRepository.getIntakeHistory(),
        historyRepository.getAdherenceStats(),
        _currentFilter,
        _isRefreshing,
        _errorMessage
    ) { logs, stats, filter, refreshing, error ->
        when {
            error != null -> HistoryUiState.Error(error)
            logs.isEmpty() -> HistoryUiState.Empty
            else -> {
                val filtered = when (filter) {
                    HistoryFilter.ALL -> logs
                    HistoryFilter.TAKEN -> logs.filter { it.status == DoseStatus.TAKEN }
                    HistoryFilter.MISSED -> logs.filter { it.status == DoseStatus.MISSED }
                }
                HistoryUiState.Content(
                    stats = stats,
                    allLogs = logs,
                    filteredLogs = filtered,
                    currentFilter = filter,
                    isRefreshing = refreshing
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState.Loading
    )

    private val _effect = MutableSharedFlow<HistoryUiEffect>()
    val effect: SharedFlow<HistoryUiEffect> = _effect.asSharedFlow()

    fun onEvent(event: HistoryUiEvent) {
        when (event) {
            is HistoryUiEvent.SetFilter -> _currentFilter.value = event.filter
            is HistoryUiEvent.Refresh -> refresh()
            is HistoryUiEvent.Retry -> _errorMessage.value = null
        }
    }

    private fun refresh() {
        viewModelScope.launch(dispatchers.io) {
            _isRefreshing.value = true
            val result = historyRepository.refreshHistory()
            _isRefreshing.value = false
            if (result.isSuccess) {
                _effect.emit(HistoryUiEffect.ShowSnackbar("Adherence history updated"))
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Failed to refresh history"
            }
        }
    }
}
