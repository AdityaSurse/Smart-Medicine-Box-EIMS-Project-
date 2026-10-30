package com.aditya.smartmedbox.feature.medicines.ui.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditMedicineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val medicinesRepository: MedicinesRepository = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { medicinesRepository.saveMedicine(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state for new medicine has default values and is not editing`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isEditing)
            assertNull(state.id)
            assertEquals("", state.name)
            assertEquals(20, state.dosageMg)
            assertEquals(2, state.selectedBayIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state with medicineId loads medicine and sets isEditing`() = runTest {
        val medicine = Medicine(
            id = "med_123",
            name = "Atorvastatin",
            strengthMg = 40,
            dosageDescription = "40 mg",
            compartmentIndex = 3,
            compartmentLabel = "Bay 03",
            currentStock = 20,
            totalStock = 30,
            formFactor = "Round Pill",
            timings = listOf("09:00 PM"),
            repeatDays = listOf("M", "W", "F"),
            isActive = true
        )
        every { medicinesRepository.getMedicineById("med_123") } returns flowOf(medicine)

        val savedStateHandle = SavedStateHandle(mapOf("medicineId" to "med_123"))
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isEditing)
            assertEquals("med_123", state.id)
            assertEquals("Atorvastatin", state.name)
            assertEquals(40, state.dosageMg)
            assertEquals(3, state.selectedBayIndex)
            assertEquals(listOf("09:00 PM"), state.timings)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DosageIncrement and DosageDecrement modify dosage within bounds`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.onEvent(AddEditMedicineUiEvent.DosageIncrement)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(25, state.dosageMg)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onEvent(AddEditMedicineUiEvent.DosageDecrement)
        viewModel.onEvent(AddEditMedicineUiEvent.DosageDecrement)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(15, state.dosageMg)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `BaySelected updates selected bay index`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.onEvent(AddEditMedicineUiEvent.BaySelected(4))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(4, state.selectedBayIndex)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `TimeAdded and TimeRemoved update timings list`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.onEvent(AddEditMedicineUiEvent.TimeAdded("01:00 PM"))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.timings.contains("01:00 PM"))
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onEvent(AddEditMedicineUiEvent.TimeRemoved("01:00 PM"))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.timings.contains("01:00 PM"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DayToggled toggles active state of selected day`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        // Day index 5 (Saturday) is default false
        viewModel.onEvent(AddEditMedicineUiEvent.DayToggled(5))
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.activeDays[5])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Save when valid calls repository saveMedicine and sets isSaved`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.onEvent(AddEditMedicineUiEvent.NameChanged("Aspirin"))
        viewModel.onEvent(AddEditMedicineUiEvent.Save)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { medicinesRepository.saveMedicine(match { it.name == "Aspirin" }) }
    }

    @Test
    fun `DismissAffirmation resets isSaved`() = runTest {
        val savedStateHandle = SavedStateHandle()
        val viewModel = AddEditMedicineViewModel(medicinesRepository, savedStateHandle)

        viewModel.onEvent(AddEditMedicineUiEvent.Save)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(AddEditMedicineUiEvent.DismissAffirmation)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isSaved)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
