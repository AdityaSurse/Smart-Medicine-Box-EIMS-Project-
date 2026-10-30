package com.aditya.smartmedbox.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme
import com.aditya.smartmedbox.feature.medicines.ui.addedit.AddEditMedicineScreen
import com.aditya.smartmedbox.feature.medicines.ui.addedit.AddEditMedicineUiEvent
import com.aditya.smartmedbox.feature.medicines.ui.addedit.AddEditMedicineUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddEditMedicineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun newSchedule_displaysConfigureHeaderAndComponents() {
        val events = mutableListOf<AddEditMedicineUiEvent>()

        composeTestRule.setContent {
            SmartMedBoxTheme {
                AddEditMedicineScreen(
                    uiState = AddEditMedicineUiState(
                        name = "Metformin",
                        dosageMg = 500,
                        selectedBayIndex = 1,
                        isEditing = false
                    ),
                    onEvent = { events.add(it) },
                    onNavigateBack = {}
                )
            }
        }

        // Header
        composeTestRule.onNodeWithText("Configure Schedule").assertIsDisplayed()

        // Increment dosage
        composeTestRule.onNodeWithContentDescription("Increase strength").performClick()
        assertTrue(events.any { it is AddEditMedicineUiEvent.DosageIncrement })

        // Select Compartment Bay 3
        composeTestRule.onNodeWithContentDescription("Compartment Bay 3, not selected").performClick()
        assertTrue(events.any { it is AddEditMedicineUiEvent.BaySelected && it.bayIndex == 3 })

        // Click Save Schedule
        composeTestRule.onNodeWithText("ARM DISPENSER SCHEDULE").performClick()
        assertTrue(events.any { it is AddEditMedicineUiEvent.Save })
    }

    @Test
    fun editMode_displaysEditRegimenHeader() {
        composeTestRule.setContent {
            SmartMedBoxTheme {
                AddEditMedicineScreen(
                    uiState = AddEditMedicineUiState(
                        name = "Atorvastatin",
                        isEditing = true
                    ),
                    onEvent = {},
                    onNavigateBack = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Edit Regimen").assertIsDisplayed()
    }
}

