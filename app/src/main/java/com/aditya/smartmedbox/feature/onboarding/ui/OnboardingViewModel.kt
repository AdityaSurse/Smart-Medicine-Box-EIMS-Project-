package com.aditya.smartmedbox.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.onboarding.domain.model.OnboardingDevice
import com.aditya.smartmedbox.feature.onboarding.domain.repository.OnboardingRepository
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
class OnboardingViewModel @Inject constructor(
    private val repository: OnboardingRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _isPairingInProgress = MutableStateFlow(false)
    private val _isPairingSuccess = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OnboardingUiState> = combine(
        repository.getDiscoveredDevice(),
        _isPairingInProgress,
        _isPairingSuccess,
        _errorMessage
    ) { device, isPairing, isSuccess, error ->
        when {
            error != null -> OnboardingUiState.Error(error)
            device == null -> OnboardingUiState.Empty
            else -> OnboardingUiState.Content(
                currentStep = 2,
                totalSteps = 3,
                device = device,
                isPairingInProgress = isPairing,
                isPairingSuccess = isSuccess
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OnboardingUiState.Loading
    )

    private val _effect = MutableSharedFlow<OnboardingUiEffect>()
    val effect: SharedFlow<OnboardingUiEffect> = _effect.asSharedFlow()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.DiscoverDevices -> discoverDevices()
            is OnboardingUiEvent.ConfirmPairing -> confirmPairing(event.code)
            is OnboardingUiEvent.Retry -> retry()
            is OnboardingUiEvent.Troubleshoot -> troubleshoot()
        }
    }

    private fun discoverDevices() {
        _errorMessage.value = null
    }

    private fun confirmPairing(code: String) {
        viewModelScope.launch(dispatchers.io) {
            _isPairingInProgress.value = true
            val result = repository.pairDevice(code)
            _isPairingInProgress.value = false
            if (result.isSuccess) {
                _isPairingSuccess.value = true
                _effect.emit(OnboardingUiEffect.ShowSnackbar("Smart Box paired successfully"))
                _effect.emit(OnboardingUiEffect.NavigateToDashboard)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Pairing failed"
            }
        }
    }

    private fun retry() {
        _errorMessage.value = null
    }

    private fun troubleshoot() {
        viewModelScope.launch(dispatchers.main) {
            _effect.emit(OnboardingUiEffect.ShowSnackbar("Ensure Smart Box is plugged in and within 3 feet."))
        }
    }
}
