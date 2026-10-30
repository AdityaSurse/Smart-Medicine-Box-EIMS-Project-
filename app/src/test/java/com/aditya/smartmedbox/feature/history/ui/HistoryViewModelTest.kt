package com.aditya.smartmedbox.feature.history.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.history.domain.model.AdherenceStats
import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog
import com.aditya.smartmedbox.feature.history.domain.repository.HistoryRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val historyRepository: HistoryRepository = mockk()

    private val logsFlow = MutableStateFlow<List<IntakeLog>>(emptyList())
    private val statsFlow = MutableStateFlow(
        AdherenceStats(
            overallAdherenceRatePercent = 94,
            totalTaken = 28,
            totalMissed = 2
        )
    )

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private val sampleLogs = listOf(
        IntakeLog(
            id = "log_1",
            medicineName = "Metformin",
            dosageInstructions = "500 mg • 1 tablet",
            timeAndBay = "08:00 AM • Bay 01",
            dateGroup = "Today",
            timestamp = 1700000000L,
            measuredDeltaWeightGrams = 0.5f,
            status = DoseStatus.TAKEN
        ),
        IntakeLog(
            id = "log_2",
            medicineName = "Lisinopril",
            dosageInstructions = "10 mg • 1 tablet",
            timeAndBay = "08:00 PM • Bay 02",
            dateGroup = "Yesterday",
            timestamp = 1699900000L,
            measuredDeltaWeightGrams = 0.0f,
            status = DoseStatus.MISSED
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { historyRepository.getIntakeHistory() } returns logsFlow
        every { historyRepository.getAdherenceStats() } returns statsFlow
        coEvery { historyRepository.refreshHistory() } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when intake logs are empty emits Empty state`() = runTest {
        logsFlow.value = emptyList()
        val viewModel = HistoryViewModel(historyRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem()
            assertTrue(state is HistoryUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when intake logs exist emits Content state`() = runTest {
        logsFlow.value = sampleLogs
        val viewModel = HistoryViewModel(historyRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(2, state.allLogs.size)
            assertEquals(2, state.filteredLogs.size)
            assertEquals(HistoryFilter.ALL, state.currentFilter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SetFilter TAKEN filters only taken doses`() = runTest {
        logsFlow.value = sampleLogs
        val viewModel = HistoryViewModel(historyRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content (ALL)

            viewModel.onEvent(HistoryUiEvent.SetFilter(HistoryFilter.TAKEN))
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(1, state.filteredLogs.size)
            assertEquals(DoseStatus.TAKEN, state.filteredLogs.first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SetFilter MISSED filters only missed doses`() = runTest {
        logsFlow.value = sampleLogs
        val viewModel = HistoryViewModel(historyRepository, dispatchers)

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Content (ALL)

            viewModel.onEvent(HistoryUiEvent.SetFilter(HistoryFilter.MISSED))
            val state = awaitItem() as HistoryUiState.Content
            assertEquals(1, state.filteredLogs.size)
            assertEquals(DoseStatus.MISSED, state.filteredLogs.first().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Refresh event calls repository refreshHistory and emits ShowSnackbar`() = runTest {
        logsFlow.value = sampleLogs
        val viewModel = HistoryViewModel(historyRepository, dispatchers)
        testScheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(HistoryUiEvent.Refresh)
            testScheduler.advanceUntilIdle()

            val snackbar = awaitItem() as HistoryUiEffect.ShowSnackbar
            assertEquals("Adherence history updated", snackbar.message)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { historyRepository.refreshHistory() }
    }
}
