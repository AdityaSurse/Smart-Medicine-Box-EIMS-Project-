package com.aditya.smartmedbox.feature.auth.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.model.User
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val authRepository: AuthRepository = mockk()
    private val authStateFlow = MutableStateFlow<User?>(null)

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getAuthState() } returns authStateFlow
        viewModel = AuthViewModel(authRepository, dispatchers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState contains default user form state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial value
            val state = awaitItem() as AuthUiState.Content
            assertEquals("eleanor.vance@apothecary.care", state.email)
            assertEquals("password123", state.password)
            assertFalse(state.isSignUpMode)
            assertFalse(state.isLoading)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EmailChanged updates email and resets error`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.EmailChanged("new.user@test.com"))
            val state = awaitItem() as AuthUiState.Content
            assertEquals("new.user@test.com", state.email)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PasswordChanged updates password and resets error`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.PasswordChanged("secret456"))
            val state = awaitItem() as AuthUiState.Content
            assertEquals("secret456", state.password)
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleAuthMode toggles isSignUpMode`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.ToggleAuthMode)
            val state = awaitItem() as AuthUiState.Content
            assertTrue(state.isSignUpMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Submit with invalid email sets error message`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.EmailChanged("not-an-email"))
            awaitItem()

            viewModel.onEvent(AuthUiEvent.Submit)
            val state = awaitItem() as AuthUiState.Content
            assertEquals("Please enter a valid email address.", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Submit with short password sets error message`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.PasswordChanged("123"))
            awaitItem()

            viewModel.onEvent(AuthUiEvent.Submit)
            val state = awaitItem() as AuthUiState.Content
            assertEquals("Password must be at least 6 characters.", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Submit in sign-in mode when repository succeeds navigates to dashboard`() = runTest {
        val user = User(uid = "user1", email = "test@example.com", displayName = "Test")
        coEvery { authRepository.signIn(any(), any()) } returns Result.success(user)

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.Submit)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AuthUiEffect.ShowSnackbar
            assertEquals("Welcome back, Eleanor", snackbar.message)
            val navigate = awaitItem() as AuthUiEffect.NavigateToDashboard
            assertNotNull(navigate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Submit in sign-up mode when repository succeeds navigates to dashboard`() = runTest {
        val user = User(uid = "user2", email = "test@example.com", displayName = "Test")
        coEvery { authRepository.signUp(any(), any()) } returns Result.success(user)

        viewModel.onEvent(AuthUiEvent.ToggleAuthMode)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.Submit)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AuthUiEffect.ShowSnackbar
            assertEquals("Welcome back, Eleanor", snackbar.message)
            val navigate = awaitItem() as AuthUiEffect.NavigateToDashboard
            assertNotNull(navigate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Submit when repository fails displays error message`() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        coEvery { authRepository.signIn(any(), any()) } throws RuntimeException("Invalid credentials")

        viewModel.onEvent(AuthUiEvent.Submit)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as AuthUiState.Content
        assertEquals("Invalid credentials", state.errorMessage)
    }

    @Test
    fun `SignInWithGoogle when succeeds navigates to dashboard`() = runTest {
        val user = User(uid = "google_user", email = "google@example.com", displayName = "Google User")
        coEvery { authRepository.signInWithGoogle("token_123") } returns Result.success(user)

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.SignInWithGoogle("token_123"))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AuthUiEffect.ShowSnackbar
            assertEquals("Signed in with Google", snackbar.message)
            val navigate = awaitItem() as AuthUiEffect.NavigateToDashboard
            assertNotNull(navigate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SignOut calls repository and shows snackbar effect`() = runTest {
        coEvery { authRepository.signOut() } returns Result.success(Unit)

        viewModel.effect.test {
            viewModel.onEvent(AuthUiEvent.SignOut)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AuthUiEffect.ShowSnackbar
            assertEquals("Signed out successfully.", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { authRepository.signOut() }
    }

    @Test
    fun `ClearError sets errorMessage to null`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            awaitItem()

            viewModel.onEvent(AuthUiEvent.PasswordChanged("12"))
            awaitItem()

            viewModel.onEvent(AuthUiEvent.Submit)
            awaitItem()

            viewModel.onEvent(AuthUiEvent.ClearError)
            val state = awaitItem() as AuthUiState.Content
            assertNull(state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
