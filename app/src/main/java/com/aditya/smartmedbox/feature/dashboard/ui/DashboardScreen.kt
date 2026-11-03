package com.aditya.smartmedbox.feature.dashboard.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxDoseCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxIntakeLogRow
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxMetricCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(uiState: DashboardUiState, onEvent: (DashboardUiEvent) -> Unit, modifier: Modifier = Modifier, onNavigateToAlerts: () -> Unit = {}) {
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
private fun DashboardContent(state: DashboardUiState.Success, onEvent: (DashboardUiEvent) -> Unit) {
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

        if (state.isExactAlarmPermissionRequired) {
            ExactAlarmPermissionBanner(
                onEnableClick = { onEvent(DashboardUiEvent.RequestExactAlarmPermission) },
                onDismissClick = { onEvent(DashboardUiEvent.DismissExactAlarmBanner) }
            )
        }

        if (state.isNotificationPermissionRequired) {
            NotificationPermissionBanner(
                onEnableClick = { onEvent(DashboardUiEvent.RequestNotificationPermission) },
                onDismissClick = { onEvent(DashboardUiEvent.DismissNotificationBanner) }
            )
        }

        if (state.isTelemetryStale) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                    .border(spacing.hairlineBorder, MaterialTheme.colorScheme.error, CardShape)
                    .padding(spacing.medium)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WarningAmber,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(spacing.iconLarge)
                        )
                        Column {
                            Text(
                                text = "Telemetry Stale: Smart Box Disconnected",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Last device ping was ${state.lastSeenText}. Verify 5V power and Wi-Fi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    TextButton(onClick = { onEvent(DashboardUiEvent.Refresh) }) {
                        Text(
                            text = "Sync",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. HARDWARE LIVE SYNCED STATUS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(
                    width = spacing.hairlineBorder,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = CardShape
                )
                .padding(horizontal = spacing.medium, vertical = spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "PulseAlpha"
                )
                val dotBaseColor = if (state.isTelemetryStale) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
                Box(
                    modifier = Modifier
                        .size(spacing.indicatorDot)
                        .clip(CircleShape)
                        .background(dotBaseColor.copy(alpha = pulseAlpha))
                )
                Text(
                    text = "ESP32-WROOM-32 • NODE_01",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sync,
                    contentDescription = null,
                    tint = if (state.isTelemetryStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(spacing.iconSmall)
                )
                Text(
                    text = if (state.isTelemetryStale) "STALE (${state.lastSeenText})" else "LIVE SYNCED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.isTelemetryStale) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 2. TOP GREETING & REGIMEN TITLE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TODAY'S REGIMEN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Good evening, ${state.telemetry.patientName}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            // Box Online / Stale Status Tag
            val indicatorColor = if (state.isTelemetryStale) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.secondary
            }
            val statusLabel = if (state.isTelemetryStale) {
                "DISCONNECTED"
            } else {
                "ONLINE"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                modifier = Modifier
                    .clip(PillShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(
                        width = spacing.hairlineBorder,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = PillShape
                    )
                    .padding(horizontal = spacing.small, vertical = spacing.micro)
                    .semantics { contentDescription = "Smart box status $statusLabel" }
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.indicatorDot)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 3. HERO NEXT DOSE CARD (WITH PRECISION CIRCULAR COUNTDOWN RING)
        SmartMedBoxDoseCard(
            medicineName = state.telemetry.nextDoseMedicine,
            dosage = "${state.telemetry.nextDoseDosage} • ${state.telemetry.nextDoseTime}",
            timeOrCountdown = state.telemetry.nextDoseCountdown,
            trayLabel = state.telemetry.nextDoseTray,
            progressFraction = state.telemetry.cycleProgressFraction,
            progressSubtext = "Last taken ${state.telemetry.lastTakenTime}" to "Due ${state.telemetry.nextDoseTime}",
            latchStatusText = "Bay latch primed for ${state.telemetry.nextDoseTime}",
            actionButtonText = "Dispense Now",
            onActionClick = { onEvent(DashboardUiEvent.DispenseNow(state.telemetry.nextDoseTray)) }
        )

        // 4. CAPSULE DOSE TIMELINE (TODAY'S SCHEDULE AT A GLANCE)
        if (state.scheduleLedger.isNotEmpty()) {
            CapsuleDoseTimeline(ledger = state.scheduleLedger)
        }

        // 5. PHYSICAL COMPARTMENT & BOX ENVIRONMENT BENTO TILES (2X2)
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                SmartMedBoxMetricCard(
                    label = "DHT22 Temp",
                    value = "${state.telemetry.temperatureCelsius}°C",
                    icon = Icons.Outlined.DeviceThermostat,
                    badgeText = "CHAMBER A",
                    subtext = "Normal (15-30°C)",
                    showStatusDot = true,
                    modifier = Modifier.weight(1f)
                )
                SmartMedBoxMetricCard(
                    label = "Humidity",
                    value = "${state.telemetry.humidityPercent.toInt()}%",
                    icon = Icons.Outlined.WaterDrop,
                    badgeText = "SAFE",
                    subtext = "Optimal (<65% RH)",
                    showStatusDot = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                val totalCapacity = 30f
                val gaugeFraction = (state.telemetry.tabletsRemaining / totalCapacity).coerceIn(0f, 1f)
                SmartMedBoxMetricCard(
                    label = "Load Cell",
                    value = "${state.telemetry.tabletsRemaining} pills",
                    icon = Icons.Outlined.Inventory2,
                    badgeText = "BAY 01-04",
                    subtext = "Gross: 42.6g",
                    verticalGaugeFraction = gaugeFraction,
                    modifier = Modifier.weight(1f)
                )
                SmartMedBoxMetricCard(
                    label = "Security",
                    value = if (state.telemetry.isBoxOpen) "Lid Open" else "Lid Closed",
                    icon = if (state.telemetry.isBoxOpen) Icons.Outlined.LockOpen else Icons.Outlined.Lock,
                    badgeText = if (state.telemetry.isBoxOpen) "BREACH" else "LATCHED",
                    subtext = if (state.telemetry.isBoxOpen) "Reed switch unsealed" else "Magnet switch locked",
                    showStatusDot = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Remote Actions Strip
        OutlinedButton(
            onClick = { onEvent(DashboardUiEvent.SilenceBuzzer) },
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.minTouchTarget),
            shape = CardShape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.VolumeOff,
                    contentDescription = null,
                    modifier = Modifier.size(spacing.iconMedium)
                )
                Text(
                    text = "Silence Smart Box Buzzer",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
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

@Composable
private fun ExactAlarmPermissionBanner(onEnableClick: () -> Unit, onDismissClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.errorContainer)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.error, CardShape)
            .padding(spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Alarm,
                contentDescription = "Exact Alarms Notice",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(spacing.large)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Exact Alarms Disabled",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Allow exact alarms to ensure critical medication alerts ring on time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            TextButton(onClick = onEnableClick) {
                Text(
                    text = "ENABLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDismissClick) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss notice",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun NotificationPermissionBanner(onEnableClick: () -> Unit, onDismissClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.tertiary, CardShape)
            .padding(spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications Notice",
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(spacing.large)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Medication Alerts Disabled",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "Enable notifications so you never miss a dose or temperature alert.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            TextButton(onClick = onEnableClick) {
                Text(
                    text = "ENABLE",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            IconButton(onClick = onDismissClick) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Dismiss notice",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun CapsuleDoseTimeline(ledger: List<DashboardScheduleItem>, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    if (ledger.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CYCLE TIMELINE • TODAY (${ledger.size} DOSES)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            val takenCount = ledger.count { it.status == DoseStatus.TAKEN }
            Text(
                text = "$takenCount OF ${ledger.size} CONSUMED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
        ) {
            ledger.take(5).forEach { item ->
                val isTaken = item.status == DoseStatus.TAKEN
                val isMissed = item.status == DoseStatus.MISSED

                val bg = when {
                    isTaken -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    isMissed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                }
                val contentColor = when {
                    isTaken -> MaterialTheme.colorScheme.onSecondaryContainer
                    isMissed -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                val badgeText = when {
                    isTaken -> "TAKEN"
                    isMissed -> "MISSED"
                    else -> "WAIT"
                }
                val icon = when {
                    isTaken -> Icons.Outlined.Check
                    isMissed -> Icons.Outlined.Close
                    else -> Icons.Outlined.Alarm
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(PillShape)
                        .background(bg)
                        .border(
                            width = spacing.hairlineBorder,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            shape = PillShape
                        )
                        .padding(horizontal = spacing.micro, vertical = spacing.small),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.large)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isTaken -> MaterialTheme.colorScheme.secondary
                                    isMissed -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isTaken || isMissed) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(spacing.iconSmall)
                        )
                    }

                    Text(
                        text = item.time,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )

                    Text(
                        text = item.medicineName.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                when {
                                    isTaken -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    isMissed -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            )
                            .padding(horizontal = spacing.extraSmall, vertical = spacing.micro)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
