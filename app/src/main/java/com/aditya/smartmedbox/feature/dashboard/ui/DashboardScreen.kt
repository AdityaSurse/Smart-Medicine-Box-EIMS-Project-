package com.aditya.smartmedbox.feature.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxDoseCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxIntakeLogRow
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxMetricCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxStatusChip
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAlerts: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "Home",
                actions = {
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.semantics { contentDescription = "Alerts and Notifications" }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Alerts",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = spacing.medium)
                            .size(spacing.extraLarge)
                            .clip(PillShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Eleanor",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(spacing.medium)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is DashboardUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Loading protocol and smart box telemetry...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DashboardUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Telemetry Disconnected",
                        message = uiState.message,
                        onRetry = { onEvent(DashboardUiEvent.Refresh) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }
                is DashboardUiState.Offline -> {
                    SmartMedBoxErrorState(
                        title = "Smart Box Offline",
                        message = "Device ping timed out. Verify Wi-Fi and power supply.",
                        onRetry = { onEvent(DashboardUiEvent.Refresh) },
                        retryButtonText = "Reconnect Box",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }
                is DashboardUiState.Success -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = { onEvent(DashboardUiEvent.Refresh) },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        DashboardContent(
                            state = uiState,
                            onEvent = onEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onEvent: (DashboardUiEvent) -> Unit
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = spacing.marginMobile),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        Spacer(modifier = Modifier.height(spacing.extraSmall))

        // 1. TOP GREETING & SMART BOX STATUS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TODAY'S PROTOCOL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Good evening, ${state.telemetry.patientName}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Box Online Status Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, PillShape)
                    .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                    .semantics { contentDescription = "Smart box status online" }
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.small)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                )
                Text(
                    text = "BOX ONLINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 2. HERO NEXT DOSE CARD
        SmartMedBoxDoseCard(
            medicineName = state.telemetry.nextDoseMedicine,
            dosage = "${state.telemetry.nextDoseDosage} • ${state.telemetry.nextDoseTime}",
            timeOrCountdown = state.telemetry.nextDoseCountdown,
            trayLabel = state.telemetry.nextDoseTray,
            progressFraction = state.telemetry.cycleProgressFraction,
            progressSubtext = "Last taken ${state.telemetry.lastTakenTime}" to "Due ${state.telemetry.nextDoseTime}",
            actionButtonText = "Dispense Now",
            onActionClick = { onEvent(DashboardUiEvent.DispenseNow(state.telemetry.nextDoseTray)) }
        )

        // 3. PHYSICAL COMPARTMENT & BOX ENVIRONMENT BENTO TILES (2X2)
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                SmartMedBoxMetricCard(
                    label = "Internal Temp",
                    value = "${state.telemetry.temperatureCelsius}°C",
                    icon = Icons.Outlined.DeviceThermostat,
                    subtext = "Optimal",
                    modifier = Modifier.weight(1f)
                )
                SmartMedBoxMetricCard(
                    label = "Humidity",
                    value = "${state.telemetry.humidityPercent.toInt()}%",
                    icon = Icons.Outlined.WaterDrop,
                    subtext = "Dry seal",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                SmartMedBoxMetricCard(
                    label = "Remaining",
                    value = "${state.telemetry.tabletsRemaining}",
                    icon = Icons.Outlined.Inventory2,
                    subtext = "tablets",
                    modifier = Modifier.weight(1f)
                )
                SmartMedBoxMetricCard(
                    label = "Cadence",
                    value = state.telemetry.cadenceStatus,
                    icon = Icons.Outlined.Verified,
                    statusIndicator = {
                        SmartMedBoxStatusChip(status = DoseStatus.ON_TIME)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. TODAY'S SCHEDULE LEDGER
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S SCHEDULE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${state.scheduleLedger.size} Doses Logged",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.scheduleLedger.isEmpty()) {
                SmartMedBoxEmptyState(
                    title = "No Doses Today",
                    message = "All prescriptions for today are cleared."
                )
            } else {
                state.scheduleLedger.forEach { item ->
                    SmartMedBoxIntakeLogRow(
                        medicineName = item.medicineName,
                        dosageAndInstructions = item.dosage,
                        timeAndTray = "${item.time} • ${item.tray}",
                        status = item.status
                    )
                }
            }
        }

        // 5. CAREGIVER EDITORIAL NOTE CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.huge)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EditNote,
                        contentDescription = "Caregiver Note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(spacing.large)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CAREGIVER NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = state.caregiverNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = state.caregiverNoteAuthor,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.extraSmall)
                    )
                }
            }
        }

        // 6. WEEKLY ADHERENCE SUMMARY LINK CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .clickable { onEvent(DashboardUiEvent.Refresh) }
                .padding(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.huge)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = "Adherence Analytics",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(spacing.large)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Text(
                                text = "Missed this week:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${state.missedThisWeekCount}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "${state.weeklyAdherencePercent}% Adherence • Box synced 2m ago",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "View Adherence History",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.large)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.medium))
    }
}
