package com.aditya.smartmedbox.feature.medicines.ui

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MedicinesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val medicinesRepository: MedicinesRepository = mockk()
    private val medicinesFlow = MutableStateFlow<List<Medicine>>(emptyList())

    private lateinit var viewModel: MedicinesViewModel

    private val sampleMeds = listOf(
        Medicine(
            id = "med_1",
            name = "Metformin",
            strengthMg = 500,
            dosageDescription = "500 mg tablet",
            compartmentIndex = 1,
            compartmentLabel = "Bay 01",
            currentStock = 28,
            totalStock = 30,
            formFactor = "Coated Tablet",
            timings = listOf("08:00 AM"),
            repeatDays = listOf("M", "W", "F"),
            isActive = true
        ),
        Medicine(
            id = "med_2",
            name = "Lisinopril",
            strengthMg = 10,
            dosageDescription = "10 mg tablet",
            compartmentIndex = 2,
            compartmentLabel = "Bay 02",
            currentStock = 3, // Low stock
            totalStock = 30,
            formFactor = "Round Pill",
            timings = listOf("08:00 PM"),
            repeatDays = listOf("M", "T", "W", "T", "F"),
            isActive = true
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { medicinesRepository.getMedicines() } returns medicinesFlow
        coEvery { medicinesRepository.toggleActive(any()) } returns Result.success(Unit)
        coEvery { medicinesRepository.deleteMedicine(any()) } returns Result.success(Unit)
        coEvery { medicinesRepository.refresh() } returns Result.success(Unit)
        viewModel = MedicinesViewModel(medicinesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state with empty repository emits Empty state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem() as MedicinesUiState.Empty
            assertFalse(state.isSearchResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repository with medications emits Success state`() = runTest {
        medicinesFlow.value = sampleMeds

        viewModel.uiState.test {
            awaitItem() // Loading
            val state = awaitItem() as MedicinesUiState.Success
            assertEquals(2, state.medicines.size)
            assertEquals(2, state.filteredMedicines.size)
            assertFalse(state.filterLowStock)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Search filter filters medications matching query`() = runTest {
        medicinesFlow.value = sampleMeds

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success initial

            viewModel.onEvent(MedicinesUiEvent.Search("Lisin"))
            val state = awaitItem() as MedicinesUiState.Success
            assertEquals(1, state.filteredMedicines.size)
            assertEquals("Lisinopril", state.filteredMedicines.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Search filter with no match emits Empty search result`() = runTest {
        medicinesFlow.value = sampleMeds

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success initial

            viewModel.onEvent(MedicinesUiEvent.Search("Aspirin"))
            val state = awaitItem() as MedicinesUiState.Empty
            assertTrue(state.isSearchResult)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleFilterLowStock filters to medications with stock 5 or less`() = runTest {
        medicinesFlow.value = sampleMeds

        viewModel.uiState.test {
            awaitItem() // Loading
            awaitItem() // Success initial

            viewModel.onEvent(MedicinesUiEvent.ToggleFilterLowStock)
            val state = awaitItem() as MedicinesUiState.Success
            assertTrue(state.filterLowStock)
            assertEquals(1, state.filteredMedicines.size)
            assertEquals("Lisinopril", state.filteredMedicines.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleActive event calls repository toggleActive`() = runTest {
        viewModel.onEvent(MedicinesUiEvent.ToggleActive("med_1"))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { medicinesRepository.toggleActive("med_1") }
    }

    @Test
    fun `DeleteMedicine event calls repository deleteMedicine`() = runTest {
        viewModel.onEvent(MedicinesUiEvent.DeleteMedicine("med_2"))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { medicinesRepository.deleteMedicine("med_2") }
    }

    @Test
    fun `Refresh event calls repository refresh`() = runTest {
        viewModel.onEvent(MedicinesUiEvent.Refresh)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { medicinesRepository.refresh() }
    }
}
