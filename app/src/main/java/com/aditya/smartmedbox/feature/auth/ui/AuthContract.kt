package com.aditya.smartmedbox.feature.auth.ui

import com.aditya.smartmedbox.feature.auth.domain.model.User

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Offline : AuthUiState

    data class Content(
        val email: String = "eleanor.vance@apothecary.care",
        val password: String = "••••••••",
        val isSignUpMode: Boolean = false,
        val isLoading: Boolean = false,
        val emailError: String? = null,
        val passwordError: String? = null,
        val errorMessage: String? = null,
        val currentUser: User? = null
    ) : AuthUiState
}

sealed interface AuthUiEvent {
    data class EmailChanged(val email: String) : AuthUiEvent
    data class PasswordChanged(val password: String) : AuthUiEvent
    data object ToggleAuthMode : AuthUiEvent
    data object Submit : AuthUiEvent
    data object SignOut : AuthUiEvent
    data object ClearError : AuthUiEvent
}

sealed interface AuthUiEffect {
    data object NavigateToDashboard : AuthUiEffect
    data class ShowSnackbar(val message: String) : AuthUiEffect
}
