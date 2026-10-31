package com.aditya.smartmedbox.feature.alerts.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert
import kotlinx.coroutines.delay

@Composable
fun AlertsScreen(uiState: AlertsUiState, onEvent: (AlertsUiEvent) -> Unit, modifier: Modifier = Modifier, onNavigateBack: () -> Unit = {}) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "Dispense Confirmation",
                subtitle = "Alerts & Notifications",
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = "Navigate back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = spacing.medium)
                            .size(spacing.extraLarge)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User profile",
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
                is AlertsUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Checking smart box telemetry and adherence alerts...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is AlertsUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Alerts Sync Failure",
                        message = uiState.message,
                        onRetry = { onEvent(AlertsUiEvent.Refresh) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }

                is AlertsUiState.Empty -> {
                    SmartMedBoxEmptyState(
                        title = "All Clear",
                        message = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is AlertsUiState.Offline -> {
                    AlertsContent(
                        alerts = uiState.alerts,
                        toastMessage = uiState.message,
                        isTakenRecorded = false,
                        onEvent = onEvent
                    )
                }

                is AlertsUiState.Success -> {
                    AlertsContent(
                        alerts = uiState.alerts,
                        toastMessage = uiState.activeToastMessage,
                        isTakenRecorded = uiState.isTakenRecorded,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertsContent(alerts: List<AdherenceAlert>, toastMessage: String?, isTakenRecorded: Boolean, onEvent: (AlertsUiEvent) -> Unit) {
    val spacing = LocalSpacing.current

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(3000)
            onEvent(AlertsUiEvent.ClearToast)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.marginMobile, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        items(alerts, key = { it.id }) { alert ->
            UrgentMissedDoseBanner(
                alert = alert,
                isTaken = isTakenRecorded,
                onTakeDose = { onEvent(AlertsUiEvent.MarkTaken(alert.id)) },
                onSnooze = { onEvent(AlertsUiEvent.Snooze(alert.id, 10)) },
                onSkip = { onEvent(AlertsUiEvent.SkipDose(alert.id, "Patient requested skip")) }
            )
        }

        // Animated feedback toast card
        item {
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (toastMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.secondaryContainer, CardShape)
                            .padding(spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(spacing.iconLarge)
                            )
                            Text(
                                text = toastMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Synchronized Lockscreen Push Notification Simulation
        item {
            LockscreenNotificationMirror(
                alert = alerts.firstOrNull(),
                onTakeDose = { alerts.firstOrNull()?.let { onEvent(AlertsUiEvent.MarkTaken(it.id)) } },
                onSnooze = { alerts.firstOrNull()?.let { onEvent(AlertsUiEvent.Snooze(it.id, 10)) } }
            )
        }
    }
}

@Composable
private fun UrgentMissedDoseBanner(alert: AdherenceAlert, isTaken: Boolean, onTakeDose: () -> Unit, onSnooze: () -> Unit, onSkip: () -> Unit) {
    val spacing = LocalSpacing.current

    // Pulsing animation for smart box chime indicator
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val alertContainerColor = MaterialTheme.colorScheme.error
    val onAlertColor = MaterialTheme.colorScheme.onError

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(alertContainerColor)
            .padding(spacing.large)
            .semantics {
                contentDescription = "Urgent missed dose alert for ${alert.medicineName}. ${alert.delayMinutes} minutes overdue."
            }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.large)) {
            // Top Audio/Vibration Signal Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(onAlertColor.copy(alpha = 0.12f))
                    .border(spacing.hairlineBorder, onAlertColor.copy(alpha = 0.25f), PillShape)
                    .padding(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.pulseDot)
                            .alpha(pulseAlpha)
                            .clip(CircleShape)
                            .background(onAlertColor)
                    )
                    Text(
                        text = "URGENT CHIME ACTIVE ON SMART BOX",
                        style = MaterialTheme.typography.labelSmall,
                        color = onAlertColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Audible alarm ringing",
                        tint = onAlertColor,
                        modifier = Modifier.size(spacing.iconMedium)
                    )
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibration active",
                        tint = onAlertColor,
                        modifier = Modifier.size(spacing.iconMedium)
                    )
                }
            }

            // Alert Header & Typography
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = "DOSE MISSED • TRAY 03",
                    style = MaterialTheme.typography.labelSmall,
                    color = onAlertColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = onAlertColor,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = onAlertColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(spacing.iconSmall)
                    )
                    Text(
                        text = "Scheduled ${alert.scheduledTime} (${alert.delayMinutes} minutes ago)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onAlertColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Medicine Detail Compartment Card (Overlay)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f))
                    .border(spacing.hairlineBorder, onAlertColor.copy(alpha = 0.25f), CardShape)
                    .padding(spacing.medium)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = alert.compartmentTag.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = onAlertColor.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(spacing.micro))
                            Text(
                                text = alert.medicineName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = onAlertColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(spacing.micro))
                            Text(
                                text = "${alert.dosage} • ${alert.instructions}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onAlertColor.copy(alpha = 0.9f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(spacing.minTouchTarget)
                                .clip(ControlShape)
                                .background(onAlertColor.copy(alpha = 0.15f))
                                .border(spacing.hairlineBorder, onAlertColor.copy(alpha = 0.25f), ControlShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = onAlertColor,
                                modifier = Modifier.size(spacing.stepNodeSize)
                            )
                        }
                    }

                    HorizontalDivider(color = onAlertColor.copy(alpha = 0.2f))

                    // Compartment Visualizer Mini Ledger (4 trays)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.indicatorDot)
                    ) {
                        alert.trays.forEach { tray ->
                            val isUnopened = tray.isMissedOrUnopened
                            val trayBg = if (isUnopened) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f)
                            }
                            val trayTextColor = if (isUnopened) {
                                alertContainerColor
                            } else {
                                onAlertColor
                            }
                            val trayTitleColor = if (isUnopened) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                onAlertColor
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(ControlShape)
                                    .background(trayBg)
                                    .border(spacing.hairlineBorder, onAlertColor.copy(alpha = 0.2f), ControlShape)
                                    .padding(vertical = spacing.small, horizontal = spacing.micro),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = tray.trayId,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = trayTextColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tray.timeOfDay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = trayTitleColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tray.statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = trayTextColor,
                                        fontWeight = if (isUnopened) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Interaction Stack
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Button(
                    onClick = onTakeDose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.heroButtonHeight),
                    shape = ControlShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    enabled = !isTaken
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isTaken) "Recorded!" else "I took it",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.heroButtonHeight),
                    shape = ControlShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = onAlertColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        brush = androidx.compose.ui.graphics.SolidColor(onAlertColor)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null
                        )
                        Text(
                            text = "Remind me in 10 min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Skip this dose (requires caregiver note)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onAlertColor.copy(alpha = 0.9f)
                    )
                }
            }

            // Hardware sync footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MEDICINE DISPENSER BOX ${alert.boxSerial}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onAlertColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SYNCED ${alert.lastSyncTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = onAlertColor.copy(alpha = 0.75f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LockscreenNotificationMirror(alert: AdherenceAlert?, onTakeDose: () -> Unit, onSnooze: () -> Unit) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.small)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LOCKSCREEN ALERT MIRROR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Push Notification",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.iconLarge)
                                .clip(ControlShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(spacing.gutterMobile)
                            )
                        }
                        Text(
                            text = "DOSEGUARD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "NOW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Emergency alert",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(spacing.iconMedium)
                    )
                }

                // Notification Content
                Text(
                    text = "Missed Dose Alert: ${alert?.medicineName ?: "Medication"}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Smart box detected lid unopened at ${alert?.scheduledTime ?: "8:00 PM"}. Tap to confirm or snooze.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Action Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Button(
                        onClick = onTakeDose,
                        modifier = Modifier
                            .weight(1f)
                            .height(spacing.controlSize),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(spacing.iconSmall)
                            )
                            Text(
                                text = "Took Dose",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .weight(1f)
                            .height(spacing.controlSize),
                        shape = PillShape,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(spacing.iconSmall)
                            )
                            Text(
                                text = "Snooze 10m",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
