package com.aditya.smartmedbox.feature.schedule.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay
import com.aditya.smartmedbox.feature.schedule.domain.repository.ScheduleRepository
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
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val scheduleRepository: ScheduleRepository = mockk()

    private val daysFlow = MutableStateFlow<List<ScheduleDay>>(emptyList())
    private val schedulesFlow = MutableStateFlow<List<PrescriptionSchedule>>(emptyList())

    private val sampleDays = listOf(
        ScheduleDay(dayLetter = "T", dayNumber = 16, isSelected = true),
        ScheduleDay(dayLetter = "F", dayNumber = 17, isSelected = false)
    )

    private val sampleSchedules = listOf(
        PrescriptionSchedule(
            id = "sch_1",
            medicineName = "Metformin",
            dosageStrength = "500 mg",
            trayBay = "Tray 01",
            dispenseTimes = listOf("08:00 AM"),
            daysOfWeek = listOf("M", "T", "W", "T", "F"),
            isActive = true
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { scheduleRepository.getScheduleDays() } returns daysFlow
        every { scheduleRepository.getSchedulesForDay(any()) } returns schedulesFlow
        coEvery { scheduleRepository.toggleScheduleActive(any(), any()) } returns Result.success(Unit)
        coEvery { scheduleRepository.addSchedule(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when schedules are empty emits Empty state`() = runTest {
        daysFlow.value = sampleDays
        schedulesFlow.value = emptyList()

        val viewModel = ScheduleViewModel(scheduleRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as ScheduleUiState.Empty
            assertEquals(16, state.selectedDayNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when schedules exist emits Success state`() = runTest {
        daysFlow.value = sampleDays
        schedulesFlow.value = sampleSchedules

        val viewModel = ScheduleViewModel(scheduleRepository)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as ScheduleUiState.Success
            assertEquals(16, state.selectedDayNumber)
            assertEquals(1, state.schedules.size)
            assertEquals("Metformin", state.schedules.first().medicineName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectDay updates selected day number`() = runTest {
        daysFlow.value = sampleDays
        schedulesFlow.value = sampleSchedules

        val viewModel = ScheduleViewModel(scheduleRepository)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(ScheduleUiEvent.SelectDay(17))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem() as ScheduleUiState.Success
            assertEquals(17, state.selectedDayNumber)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleActive calls repository toggleScheduleActive`() = runTest {
        daysFlow.value = sampleDays
        schedulesFlow.value = sampleSchedules

        val viewModel = ScheduleViewModel(scheduleRepository)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(ScheduleUiEvent.ToggleActive("sch_1", false))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { scheduleRepository.toggleScheduleActive("sch_1", false) }
    }

    @Test
    fun `SaveSchedule calls repository addSchedule and sets feedback`() = runTest {
        daysFlow.value = sampleDays
        schedulesFlow.value = sampleSchedules

        val viewModel = ScheduleViewModel(scheduleRepository)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(
            ScheduleUiEvent.SaveSchedule(
                medicineName = "Atorvastatin",
                dosageStrength = "20 mg",
                compartmentBay = "Bay 02",
                dispenseTimes = listOf("09:00 PM"),
                repeatDays = listOf("M", "W", "F"),
                totalSupply = 30,
                formFactor = "Round Pill"
            )
        )
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { scheduleRepository.addSchedule(match { it.medicineName == "Atorvastatin" }) }

        val state = viewModel.uiState.value as ScheduleUiState.Success
        assertNotNull(state.userFeedbackMessage)
        assertEquals("Scheduled & Dispenser Armed", state.userFeedbackMessage)
    }
}
