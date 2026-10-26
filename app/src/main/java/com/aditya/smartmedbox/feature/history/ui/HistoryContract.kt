package com.aditya.smartmedbox.feature.history.ui

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Success(val logs: List<String> = emptyList()) : HistoryUiState
}

sealed interface HistoryUiEvent {
    data object LoadHistory : HistoryUiEvent
}
