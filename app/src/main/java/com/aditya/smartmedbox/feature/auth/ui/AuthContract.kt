package com.aditya.smartmedbox.feature.auth.ui

sealed interface AuthUiState {
    data object Idle : AuthUiState

    data object Loading : AuthUiState

    data class Authenticated(val userId: String, val email: String) : AuthUiState

    data class Error(val message: String) : AuthUiState
}

sealed interface AuthUiEvent {
    data class SignIn(val email: String) : AuthUiEvent

    data object SignOut : AuthUiEvent
}

sealed interface AuthUiEffect {
    data object NavigateToDashboard : AuthUiEffect

    data class ShowToast(val message: String) : AuthUiEffect
}
