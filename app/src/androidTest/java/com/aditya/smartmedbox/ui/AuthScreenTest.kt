package com.aditya.smartmedbox.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme
import com.aditya.smartmedbox.feature.auth.ui.AuthScreen
import com.aditya.smartmedbox.feature.auth.ui.AuthUiEvent
import com.aditya.smartmedbox.feature.auth.ui.AuthUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_displaysLoadingMessage() {
        composeTestRule.setContent {
            SmartMedBoxTheme {
                AuthScreen(
                    uiState = AuthUiState.Loading,
                    onEvent = {},
                    onNavigateToDashboard = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Verifying apothecary credentials...").assertIsDisplayed()
    }

    @Test
    fun contentState_rendersInputsAndSubmitTriggersEvent() {
        val events = mutableListOf<AuthUiEvent>()

        composeTestRule.setContent {
            SmartMedBoxTheme {
                AuthScreen(
                    uiState = AuthUiState.Content(
                        email = "user@test.com",
                        password = "password123",
                        isSignUpMode = false,
                        isLoading = false
                    ),
                    onEvent = { events.add(it) },
                    onNavigateToDashboard = {}
                )
            }
        }

        // Verify elements rendered
        composeTestRule.onNodeWithText("Access & Authentication").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In to Continue").assertIsDisplayed()

        // Click Sign In
        composeTestRule.onNodeWithText("Sign In to Continue").performClick()
        assertTrue(events.any { it is AuthUiEvent.Submit })

        // Switch to sign up mode
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").performClick()
        assertTrue(events.any { it is AuthUiEvent.ToggleAuthMode })
    }

    @Test
    fun contentState_displaysErrorMessageWhenPresent() {
        composeTestRule.setContent {
            SmartMedBoxTheme {
                AuthScreen(
                    uiState = AuthUiState.Content(
                        errorMessage = "Invalid email or password."
                    ),
                    onEvent = {},
                    onNavigateToDashboard = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Invalid email or password.").assertIsDisplayed()
    }
}
