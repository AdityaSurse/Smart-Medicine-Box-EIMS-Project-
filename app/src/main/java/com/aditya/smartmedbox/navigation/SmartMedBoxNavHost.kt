package com.aditya.smartmedbox.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aditya.smartmedbox.feature.alerts.ui.AlertsScreen
import com.aditya.smartmedbox.feature.auth.ui.AuthScreen
import com.aditya.smartmedbox.feature.auth.ui.AuthUiState
import com.aditya.smartmedbox.feature.dashboard.ui.DashboardScreen
import com.aditya.smartmedbox.feature.dashboard.ui.DashboardViewModel
import com.aditya.smartmedbox.feature.history.ui.HistoryScreen
import com.aditya.smartmedbox.feature.medicines.ui.MedicinesScreen
import com.aditya.smartmedbox.feature.schedule.ui.ScheduleScreen
import com.aditya.smartmedbox.feature.settings.ui.SettingsScreen

@Composable
fun SmartMedBoxAppScreen(navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAuthOrOnboarding = currentRoute?.contains("Auth") == true || currentRoute?.contains("Onboarding") == true

    Scaffold(
        bottomBar = {
            if (!isAuthOrOnboarding) {
                SmartMedBoxBottomBar(navController = navController)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Onboarding> {
                val viewModel: com.aditya.smartmedbox.feature.onboarding.ui.OnboardingViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.aditya.smartmedbox.feature.onboarding.ui.OnboardingScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }

            composable<Screen.Auth> {
                val viewModel: com.aditya.smartmedbox.feature.auth.ui.AuthViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.aditya.smartmedbox.feature.auth.ui.AuthScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard) {
                            popUpTo<Screen.Auth> { inclusive = true }
                        }
                    }
                )
            }

            composable<Screen.Dashboard> {
                val viewModel: DashboardViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                DashboardScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }

            composable<Screen.Schedule> {
                ScheduleScreen()
            }

            composable<Screen.Medicines> {
                val viewModel: com.aditya.smartmedbox.feature.medicines.ui.MedicinesViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.aditya.smartmedbox.feature.medicines.ui.MedicinesScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onNavigateToAddMedicine = { navController.navigate(Screen.AddEditMedicine()) },
                    onNavigateToEditMedicine = { medId -> navController.navigate(Screen.AddEditMedicine(medId)) }
                )
            }

            composable<Screen.History> {
                val viewModel: com.aditya.smartmedbox.feature.history.ui.HistoryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                com.aditya.smartmedbox.feature.history.ui.HistoryScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }

            composable<Screen.Alerts> {
                AlertsScreen()
            }

            composable<Screen.Settings> {
                SettingsScreen()
            }
        }
    }
}
