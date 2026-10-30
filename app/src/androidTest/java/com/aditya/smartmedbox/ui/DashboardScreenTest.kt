package com.aditya.smartmedbox.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme
import com.aditya.smartmedbox.feature.dashboard.ui.DashboardScreen
import com.aditya.smartmedbox.feature.dashboard.ui.DashboardUiEvent
import com.aditya.smartmedbox.feature.dashboard.ui.DashboardUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_displaysLoadingIndicator() {
        composeTestRule.setContent {
            SmartMedBoxTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Loading,
                    onEvent = {},
                    onNavigateToAlerts = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Loading protocol and smart box telemetry...").assertIsDisplayed()
    }

    @Test
    fun successState_displaysStatusAndSilenceBuzzerAction() {
        val events = mutableListOf<DashboardUiEvent>()

        composeTestRule.setContent {
            SmartMedBoxTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(
                        lastSeenText = "2m ago",
                        isTelemetryStale = false
                    ),
                    onEvent = { events.add(it) },
                    onNavigateToAlerts = {}
                )
            }
        }

        composeTestRule.onNodeWithText("ONLINE • 2m ago").assertIsDisplayed()

        // Silence Buzzer button
        composeTestRule.onNodeWithText("Silence Smart Box Buzzer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Silence Smart Box Buzzer").performClick()

        assertTrue(events.any { it is DashboardUiEvent.SilenceBuzzer })
    }

    @Test
    fun successState_withStaleTelemetry_displaysWarningBanner() {
        composeTestRule.setContent {
            SmartMedBoxTheme {
                DashboardScreen(
                    uiState = DashboardUiState.Success(
                        lastSeenText = "12m ago",
                        isTelemetryStale = true
                    ),
                    onEvent = {},
                    onNavigateToAlerts = {}
                )
            }
        }

        composeTestRule.onNodeWithText("STALE • 12m ago").assertIsDisplayed()
        composeTestRule.onNodeWithText("Telemetry Stale: Smart Box Disconnected").assertIsDisplayed()
    }
}
