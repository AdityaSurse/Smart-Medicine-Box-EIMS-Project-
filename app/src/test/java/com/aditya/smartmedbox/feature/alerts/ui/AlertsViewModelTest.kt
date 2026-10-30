package com.aditya.smartmedbox.feature.alerts.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert
import com.aditya.smartmedbox.feature.alerts.domain.model.AlertSeverity
import com.aditya.smartmedbox.feature.alerts.domain.repository.AlertsRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlertsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val alertsRepository: AlertsRepository = mockk()
    private val alertsFlow = MutableStateFlow<List<AdherenceAlert>>(emptyList())

    private val sampleAlert = AdherenceAlert(
        id = "alert_1",
        title = "Missed Dose Alert",
        medicineName = "Metformin",
        dosage = "500 mg",
        instructions = "Take 1 tablet with water",
        compartmentTag = "Bay 01",
        scheduledTime = "08:00 AM",
        delayMinutes = 15,
        severity = AlertSeverity.CRITICAL,
        isChimeActive = true
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { alertsRepository.getActiveAlerts() } returns alertsFlow
        coEvery { alertsRepository.markDoseTaken(any()) } returns Result.success(Unit)
        coEvery { alertsRepository.snoozeAlert(any(), any()) } returns Result.success(Unit)
        coEvery { alertsRepository.skipDose(any(), any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when alerts are empty emits Empty state`() = runTest {
        alertsFlow.value = emptyList()
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is AlertsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when alerts exist emits Success state`() = runTest {
        alertsFlow.value = listOf(sampleAlert)
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as AlertsUiState.Success
            assertEquals(1, state.alerts.size)
            assertEquals("Metformin", state.alerts.first().medicineName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `MarkTaken calls repository markDoseTaken and emits ShowSnackbar effect`() = runTest {
        alertsFlow.value = listOf(sampleAlert)
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(AlertsUiEvent.MarkTaken("alert_1"))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AlertsUiEffect.ShowSnackbar
            assertEquals("Dose recorded as taken", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { alertsRepository.markDoseTaken("alert_1") }
    }

    @Test
    fun `Snooze calls repository snoozeAlert and emits ShowSnackbar effect`() = runTest {
        alertsFlow.value = listOf(sampleAlert)
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(AlertsUiEvent.Snooze("alert_1", 10))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AlertsUiEffect.ShowSnackbar
            assertEquals("Alert snoozed for 10 minutes", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { alertsRepository.snoozeAlert("alert_1", 10) }
    }

    @Test
    fun `SkipDose calls repository skipDose and emits ShowSnackbar effect`() = runTest {
        alertsFlow.value = listOf(sampleAlert)
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiEffect.test {
            viewModel.onEvent(AlertsUiEvent.SkipDose("alert_1", "Fasting blood test"))
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as AlertsUiEffect.ShowSnackbar
            assertEquals("Dose skipped. Caregiver notified.", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { alertsRepository.skipDose("alert_1", "Fasting blood test") }
    }

    @Test
    fun `ClearToast clears active toast message`() = runTest {
        alertsFlow.value = listOf(sampleAlert)
        val viewModel = AlertsViewModel(alertsRepository)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(AlertsUiEvent.MarkTaken("alert_1"))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as AlertsUiState.Success
            assertNotNull(state.activeToastMessage)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onEvent(AlertsUiEvent.ClearToast)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as AlertsUiState.Success
            assertNull(state.activeToastMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
