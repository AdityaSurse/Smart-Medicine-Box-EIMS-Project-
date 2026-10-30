package com.aditya.smartmedbox.feature.dashboard.ui

import android.content.Context
import app.cash.turbine.test
import com.aditya.smartmedbox.core.notification.NotificationPermissionChecker
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import com.aditya.smartmedbox.feature.dashboard.domain.usecase.ComputeTimeLeftUseCase
import com.aditya.smartmedbox.feature.dashboard.domain.usecase.GetNextDoseUseCase
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.usecase.GetWeeklyMissedCountUseCase
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import com.aditya.smartmedbox.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val telemetryRepository: TelemetryRepository = mockk()
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase = mockk()
    private val getNextDoseUseCase: GetNextDoseUseCase = mockk()
    private val getWeeklyMissedCountUseCase: GetWeeklyMissedCountUseCase = mockk()
    private val computeTimeLeftUseCase: ComputeTimeLeftUseCase = mockk()
    private val doseAlarmScheduler: DoseAlarmScheduler = mockk()
    private val notificationPermissionChecker: NotificationPermissionChecker = mockk()
    private val context: Context = mockk(relaxed = true)

    private val dispatchers = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
        override val unconfined = testDispatcher
    }

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { telemetryRepository.observeTelemetry(any()) } returns flowOf(
            DeviceTelemetry(deviceId = "DG-4081", isOnline = true, cadenceStatus = "On time")
        )
        every { getTodayScheduleUseCase(any()) } returns flowOf(emptyList())
        every { getNextDoseUseCase(any()) } returns flowOf(null)
        every { getWeeklyMissedCountUseCase(any()) } returns flowOf(0)
        every { computeTimeLeftUseCase(any(), any()) } returns "2h 15m"
        every { doseAlarmScheduler.canScheduleExactAlarms() } returns true
        every { notificationPermissionChecker.hasNotificationPermission(any()) } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        return DashboardViewModel(
            telemetryRepository = telemetryRepository,
            getTodayScheduleUseCase = getTodayScheduleUseCase,
            getNextDoseUseCase = getNextDoseUseCase,
            getWeeklyMissedCountUseCase = getWeeklyMissedCountUseCase,
            computeTimeLeftUseCase = computeTimeLeftUseCase,
            doseAlarmScheduler = doseAlarmScheduler,
            context = context,
            dispatchers = dispatchers,
            notificationPermissionChecker = notificationPermissionChecker
        )
    }

    @Test
    fun `uiState emits Success with On time status when no missed or pending overdue doses`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            // initial Loading
            assertEquals(DashboardUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue(item is DashboardUiState.Success)
            val success = item as DashboardUiState.Success
            assertEquals("On time", success.telemetry.cadenceStatus)
            assertEquals(0, success.missedThisWeekCount)
            assertEquals(100, success.weeklyAdherencePercent)
            assertFalse(success.isExactAlarmPermissionRequired)
        }
    }

    @Test
    fun `uiState evaluates cadence to Dose missed when weekly missed count is greater than zero`() = runTest {
        every { getWeeklyMissedCountUseCase(any()) } returns flowOf(2)

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue(item is DashboardUiState.Success)
            val success = item as DashboardUiState.Success
            assertEquals("Dose missed", success.telemetry.cadenceStatus)
            assertEquals(2, success.missedThisWeekCount)
        }
    }

    @Test
    fun `uiState evaluates cadence to Dose missed when a dose today is MISSED`() = runTest {
        val missedEvent = DoseEvent(
            id = "evt_missed",
            scheduleId = "sch_1",
            medicineId = "med_1",
            medicineName = "Amoxicillin",
            dosage = "1 tablet",
            scheduledTimeEpochMillis = System.currentTimeMillis() - 3600_000L,
            status = DoseStatus.MISSED
        )
        every { getTodayScheduleUseCase(any()) } returns flowOf(listOf(missedEvent))

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue(item is DashboardUiState.Success)
            val success = item as DashboardUiState.Success
            assertEquals("Dose missed", success.telemetry.cadenceStatus)
            assertEquals(1, success.scheduleLedger.size)
        }
    }

    @Test
    fun `uiState flags isExactAlarmPermissionRequired when scheduler cannot schedule exact alarms`() = runTest {
        every { doseAlarmScheduler.canScheduleExactAlarms() } returns false

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue(item is DashboardUiState.Success)
            val success = item as DashboardUiState.Success
            assertTrue(success.isExactAlarmPermissionRequired)
        }
    }

    @Test
    fun `onEvent RequestExactAlarmPermission emits OpenExactAlarmSettings effect`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onEvent(DashboardUiEvent.RequestExactAlarmPermission)
            assertEquals(DashboardUiEffect.OpenExactAlarmSettings, awaitItem())
        }
    }

    @Test
    fun `onEvent DismissExactAlarmBanner hides permission banner in Success state`() = runTest {
        every { doseAlarmScheduler.canScheduleExactAlarms() } returns false

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            val item = awaitItem() as DashboardUiState.Success
            assertTrue(item.isExactAlarmPermissionRequired)

            viewModel.onEvent(DashboardUiEvent.DismissExactAlarmBanner)

            val updatedItem = awaitItem() as DashboardUiState.Success
            assertFalse(updatedItem.isExactAlarmPermissionRequired)
        }
    }

    @Test
    fun `uiState flags isNotificationPermissionRequired when permission is not granted`() = runTest {
        every { notificationPermissionChecker.hasNotificationPermission(any()) } returns false

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            val item = awaitItem()
            assertTrue(item is DashboardUiState.Success)
            val success = item as DashboardUiState.Success
            assertTrue(success.isNotificationPermissionRequired)
        }
    }

    @Test
    fun `onEvent RequestNotificationPermission emits NavigateToNotificationRationale effect`() = runTest {
        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onEvent(DashboardUiEvent.RequestNotificationPermission)
            assertEquals(DashboardUiEffect.NavigateToNotificationRationale, awaitItem())
        }
    }

    @Test
    fun `onEvent DismissNotificationBanner hides notification permission banner in Success state`() = runTest {
        every { notificationPermissionChecker.hasNotificationPermission(any()) } returns false

        viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            val item = awaitItem() as DashboardUiState.Success
            assertTrue(item.isNotificationPermissionRequired)

            viewModel.onEvent(DashboardUiEvent.DismissNotificationBanner)

            val updatedItem = awaitItem() as DashboardUiState.Success
            assertFalse(updatedItem.isNotificationPermissionRequired)
        }
    }
}
