package com.aditya.smartmedbox.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingIndicator
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing

@Composable
fun DashboardScreen(uiState: DashboardUiState, onEvent: (DashboardUiEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "Smart Medicine Box",
                actions = {
                    IconButton(onClick = { onEvent(DashboardUiEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Telemetry"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (uiState) {
            is DashboardUiState.Loading -> {
                SmartMedBoxLoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
            is DashboardUiState.Error -> {
                DashboardErrorContent(
                    message = uiState.message,
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is DashboardUiState.Success -> {
                Column(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(spacing.medium),
                    verticalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    NextDoseCard(
                        medicationName = uiState.nextMedicationName,
                        scheduledDose = uiState.nextScheduledDose
                    )
                    TelemetryCard(telemetry = uiState.deviceTelemetry)
                }
            }
        }
    }
}

@Composable
private fun NextDoseCard(medicationName: String, scheduledDose: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Text(
                text = "Next Scheduled Dose",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = medicationName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = scheduledDose,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun TelemetryCard(telemetry: DeviceTelemetryUi, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Card(
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(spacing.medium)) {
            Text(
                text = "Device Telemetry",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weight: ${telemetry.currentWeightGrams}g",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = if (telemetry.isBoxOpen) "Box: OPEN" else "Box: CLOSED",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Temp: ${telemetry.temperatureCelsius}°C",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Humidity: ${telemetry.humidityPercent}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DashboardErrorContent(message: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Column(
        modifier =
        modifier
            .fillMaxSize()
            .padding(spacing.medium),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
