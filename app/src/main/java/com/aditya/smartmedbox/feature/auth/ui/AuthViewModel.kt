package com.aditya.smartmedbox.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
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
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _email = MutableStateFlow("eleanor.vance@apothecary.care")
    private val _password = MutableStateFlow("password123")
    private val _isSignUpMode = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private data class FormState(
        val email: String,
        val password: String,
        val isSignUpMode: Boolean,
        val isLoading: Boolean,
        val errorMessage: String?
    )

    private val formState = combine(
        _email,
        _password,
        _isSignUpMode,
        _isLoading,
        _errorMessage
    ) { email, password, isSignUp, loading, error ->
        FormState(email, password, isSignUp, loading, error)
    }

    val uiState: StateFlow<AuthUiState> = combine(
        authRepository.getAuthState(),
        formState
    ) { user, form ->
        AuthUiState.Content(
            email = form.email,
            password = form.password,
            isSignUpMode = form.isSignUpMode,
            isLoading = form.isLoading,
            errorMessage = form.errorMessage,
            currentUser = user
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthUiState.Content()
    )

    private val _effect = MutableSharedFlow<AuthUiEffect>()
    val effect: SharedFlow<AuthUiEffect> = _effect.asSharedFlow()

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> {
                _email.value = event.email
                _errorMessage.value = null
            }
            is AuthUiEvent.PasswordChanged -> {
                _password.value = event.password
                _errorMessage.value = null
            }
            is AuthUiEvent.ToggleAuthMode -> {
                _isSignUpMode.value = !_isSignUpMode.value
                _errorMessage.value = null
            }
            is AuthUiEvent.Submit -> submit()
            is AuthUiEvent.SignOut -> signOut()
            is AuthUiEvent.ClearError -> _errorMessage.value = null
        }
    }

    private fun submit() {
        val email = _email.value.trim()
        val password = _password.value

        if (!email.contains("@")) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _isLoading.value = true
            val result = if (_isSignUpMode.value) {
                authRepository.signUp(email, password)
            } else {
                authRepository.signIn(email, password)
            }
            _isLoading.value = false

            if (result.isSuccess) {
                _effect.emit(AuthUiEffect.ShowSnackbar("Welcome back, Eleanor"))
                _effect.emit(AuthUiEffect.NavigateToDashboard)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Authentication failed."
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch(dispatchers.io) {
            authRepository.signOut()
            _effect.emit(AuthUiEffect.ShowSnackbar("Signed out successfully."))
        }
    }
}
