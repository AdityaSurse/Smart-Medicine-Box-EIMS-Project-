package com.aditya.smartmedbox.feature.onboarding.ui

import com.aditya.smartmedbox.feature.onboarding.domain.model.OnboardingDevice

sealed interface OnboardingUiState {
    data object Loading : OnboardingUiState
    data object Empty : OnboardingUiState
    data object Offline : OnboardingUiState
    data class Error(val message: String) : OnboardingUiState

    data class Content(
        val currentStep: Int = 2,
        val totalSteps: Int = 3,
        val device: OnboardingDevice = OnboardingDevice(),
        val isPairingInProgress: Boolean = false,
        val isPairingSuccess: Boolean = false
    ) : OnboardingUiState
}

sealed interface OnboardingUiEvent {
    data object DiscoverDevices : OnboardingUiEvent
    data class ConfirmPairing(val code: String) : OnboardingUiEvent
    data object Retry : OnboardingUiEvent
    data object Troubleshoot : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data object NavigateToDashboard : OnboardingUiEffect
    data class ShowSnackbar(val message: String) : OnboardingUiEffect
}
