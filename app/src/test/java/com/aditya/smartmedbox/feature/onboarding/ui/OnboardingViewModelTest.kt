package com.aditya.smartmedbox.feature.onboarding.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.onboarding.domain.model.OnboardingDevice
import com.aditya.smartmedbox.feature.onboarding.domain.repository.OnboardingRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val onboardingRepository: OnboardingRepository = mockk()
    private val deviceFlow = MutableStateFlow<OnboardingDevice?>(null)

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private val sampleDevice = OnboardingDevice(
        id = "DG-4081",
        name = "Apothecary Smart Box",
        firmwareVersion = "v3.2",
        pairingCode = "8820",
        signalStrength = "Strong Signal",
        isSignalLocked = true,
        isPaired = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { onboardingRepository.getDiscoveredDevice() } returns deviceFlow
        coEvery { onboardingRepository.pairDevice(any()) } returns Result.success(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when no device is discovered emits Empty state`() = runTest {
        deviceFlow.value = null
        val viewModel = OnboardingViewModel(onboardingRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem()
            assertTrue(state is OnboardingUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when device is discovered emits Content state`() = runTest {
        deviceFlow.value = sampleDevice
        val viewModel = OnboardingViewModel(onboardingRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem() as OnboardingUiState.Content
            assertEquals("DG-4081", state.device.id)
            assertEquals("Apothecary Smart Box", state.device.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ConfirmPairing when success emits ShowSnackbar and NavigateToDashboard effects`() = runTest {
        deviceFlow.value = sampleDevice
        val viewModel = OnboardingViewModel(onboardingRepository, dispatchers)

        viewModel.effect.test {
            viewModel.onEvent(OnboardingUiEvent.ConfirmPairing("8820"))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as OnboardingUiEffect.ShowSnackbar
            assertEquals("Smart Box paired successfully", snackbar.message)
            val navigate = awaitItem() as OnboardingUiEffect.NavigateToDashboard
            assertNotNull(navigate)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { onboardingRepository.pairDevice("8820") }
    }

    @Test
    fun `ConfirmPairing when failure sets error message`() = runTest {
        deviceFlow.value = sampleDevice
        coEvery { onboardingRepository.pairDevice(any()) } throws RuntimeException("Pairing handshake timeout")

        val viewModel = OnboardingViewModel(onboardingRepository, dispatchers)
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }

        viewModel.onEvent(OnboardingUiEvent.ConfirmPairing("8820"))
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as OnboardingUiState.Error
        assertEquals("Pairing handshake timeout", state.message)
    }

    @Test
    fun `Troubleshoot emits snackbar instruction effect`() = runTest {
        deviceFlow.value = sampleDevice
        val viewModel = OnboardingViewModel(onboardingRepository, dispatchers)

        viewModel.effect.test {
            viewModel.onEvent(OnboardingUiEvent.Troubleshoot)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as OnboardingUiEffect.ShowSnackbar
            assertTrue(snackbar.message.contains("Ensure Smart Box is plugged in"))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
