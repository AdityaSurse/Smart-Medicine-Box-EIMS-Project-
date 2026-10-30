package com.aditya.smartmedbox.feature.settings.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val settingsRepository: SettingsRepository = mockk()
    private val authRepository: AuthRepository = mockk()

    private val settingsFlow = MutableStateFlow(
        DeviceSettings(
            snoozeIntervalMinutes = 10,
            refillThresholdTablets = 5,
            isDarkMode = false
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getSettings() } returns settingsFlow
        coEvery { settingsRepository.updateSnoozeInterval(any()) } returns Result.success(Unit)
        coEvery { settingsRepository.updateRefillThreshold(any()) } returns Result.success(Unit)
        coEvery { settingsRepository.updateThemeMode(any()) } returns Result.success(Unit)
        coEvery { settingsRepository.playTestChime() } returns Result.success(Unit)
        coEvery { authRepository.signOut() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state observes settings and emits Success`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as SettingsUiState.Success
            assertEquals(10, state.settings.snoozeIntervalMinutes)
            assertEquals(5, state.settings.refillThresholdTablets)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SetSnoozeInterval calls repository and emits ShowSnackbar effect`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.SetSnoozeInterval(15))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as SettingsUiEffect.ShowSnackbar
            assertEquals("Snooze interval updated to 15 min", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { settingsRepository.updateSnoozeInterval(15) }
    }

    @Test
    fun `SetRefillThreshold calls repository and updates feedback`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetRefillThreshold(7))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as SettingsUiState.Success
            assertNotNull(state.feedbackMessage)
            assertEquals("Refill alert threshold set to 7 tablets", state.feedbackMessage)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { settingsRepository.updateRefillThreshold(7) }
    }

    @Test
    fun `ToggleTheme calls repository and emits ShowSnackbar effect`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.ToggleTheme(true))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as SettingsUiEffect.ShowSnackbar
            assertEquals("Appearance updated", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { settingsRepository.updateThemeMode(true) }
    }

    @Test
    fun `PlayTestChime calls repository and emits ShowSnackbar effect`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.PlayTestChime)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as SettingsUiEffect.ShowSnackbar
            assertEquals("Smart Box chime triggered", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { settingsRepository.playTestChime() }
    }

    @Test
    fun `SignOut calls authRepository signOut and emits ShowSnackbar effect`() = runTest {
        val viewModel = SettingsViewModel(settingsRepository, authRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(SettingsUiEvent.SignOut)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as SettingsUiEffect.ShowSnackbar
            assertEquals("Signed out successfully", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { authRepository.signOut() }
    }
}
