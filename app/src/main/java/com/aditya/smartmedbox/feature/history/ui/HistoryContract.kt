package com.aditya.smartmedbox.feature.history.ui

import com.aditya.smartmedbox.feature.history.domain.model.AdherenceStats
import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog

enum class HistoryFilter {
    ALL,
    TAKEN,
    MISSED
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data object Offline : HistoryUiState
    data class Error(val message: String) : HistoryUiState

    data class Content(
        val stats: AdherenceStats = AdherenceStats(),
        val allLogs: List<IntakeLog> = emptyList(),
        val filteredLogs: List<IntakeLog> = emptyList(),
        val currentFilter: HistoryFilter = HistoryFilter.ALL,
        val isRefreshing: Boolean = false
    ) : HistoryUiState
}

sealed interface HistoryUiEvent {
    data class SetFilter(val filter: HistoryFilter) : HistoryUiEvent
    data object Refresh : HistoryUiEvent
    data object Retry : HistoryUiEvent
}

sealed interface HistoryUiEffect {
    data class ShowSnackbar(val message: String) : HistoryUiEffect
}
