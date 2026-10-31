package com.aditya.smartmedbox.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.aditya.smartmedbox.R
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToPairDevice: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {}
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "Settings",
                actions = {
                    IconButton(
                        onClick = onNavigateToAlerts,
                        modifier = Modifier.semantics { contentDescription = "Alerts and Notifications" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                is SettingsUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Loading smart box hardware configurations and settings...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is SettingsUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Settings Sync Error",
                        message = uiState.message,
                        onRetry = { onEvent(SettingsUiEvent.Refresh) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }

                is SettingsUiState.Offline -> {
                    SettingsContent(
                        settings = uiState.settings,
                        feedbackMessage = uiState.message,
                        onEvent = onEvent,
                        onNavigateToPairDevice = onNavigateToPairDevice,
                        onNavigateToPrivacy = onNavigateToPrivacy
                    )
                }

                is SettingsUiState.Empty -> {
                    SmartMedBoxEmptyState(
                        title = "No Configuration Found",
                        message = uiState.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is SettingsUiState.Success -> {
                    SettingsContent(
                        settings = uiState.settings,
                        feedbackMessage = uiState.feedbackMessage,
                        onEvent = onEvent,
                        onNavigateToPairDevice = onNavigateToPairDevice,
                        onNavigateToPrivacy = onNavigateToPrivacy
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: DeviceSettings,
    feedbackMessage: String?,
    onEvent: (SettingsUiEvent) -> Unit,
    onNavigateToPairDevice: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val spacing = LocalSpacing.current

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2500)
            onEvent(SettingsUiEvent.ClearFeedback)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.marginMobile, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SYSTEM & DISPENSER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, PillShape)
                        .padding(horizontal = spacing.medium, vertical = spacing.extraSmall)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.small)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = "BAY ONLINE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. Connected Device Card
        item {
            ConnectedDeviceSection(
                settings = settings,
                onPairNewBay = onNavigateToPairDevice
            )
        }

        // 2. Hardware & Network Settings
        item {
            HardwareNetworkSection(
                settings = settings,
                onSelectSnooze = { onEvent(SettingsUiEvent.SetSnoozeInterval(it)) },
                onTestTone = { onEvent(SettingsUiEvent.PlayTestChime) }
            )
        }

        // 3. Clinical Protocols & Timing
        item {
            ClinicalProtocolsSection(
                settings = settings,
                onSelectUnitSystem = { onEvent(SettingsUiEvent.SetUnitSystem(it)) },
                onSelectGracePeriod = { onEvent(SettingsUiEvent.SetGracePeriod(it)) },
                onSelectReminderRepeat = { onEvent(SettingsUiEvent.SetReminderRepeat(it)) },
                onToggleQuietHours = { onEvent(SettingsUiEvent.ToggleQuietHours(it)) }
            )
        }

        // 4. Clinical Audit & Export
        item {
            ClinicalExportSection(
                onExportCsv = { onEvent(SettingsUiEvent.ExportHistoryCsv) },
                onExportSummary = { onEvent(SettingsUiEvent.ExportHistorySummary) }
            )
        }

        // 5. Care Circle & Telemetry (Caregiver Contacts)
        item {
            CareCircleSection(settings = settings)
        }

        // 6. Refill & Inventory Thresholds
        item {
            RefillThresholdSection(
                currentThreshold = settings.refillThresholdTablets,
                onThresholdChange = { onEvent(SettingsUiEvent.SetRefillThreshold(it)) }
            )
        }

        // 7. Visual Environment (Theme toggle)
        item {
            VisualEnvironmentSection(
                isDark = settings.isDarkMode,
                onToggleTheme = { onEvent(SettingsUiEvent.ToggleTheme(it)) }
            )
        }

        // 8. Privacy & Health Data Sovereignty
        item {
            PrivacyAndDataSection(
                onNavigateToPrivacy = onNavigateToPrivacy,
                onClearAllData = { onEvent(SettingsUiEvent.ClearAllData) }
            )
        }

        // 9. System Information & Seal
        item {
            SystemInfoSection(
                settings = settings,
                onSignOut = { onEvent(SettingsUiEvent.SignOut) }
            )
        }
    }
}

@Composable
private fun ConnectedDeviceSection(settings: DeviceSettings, onPairNewBay: () -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.huge)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(spacing.large)
                        )
                    }

                    Column {
                        Text(
                            text = settings.deviceName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(spacing.indicatorDot)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Text(
                                text = "Online & Synced",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = spacing.small, vertical = spacing.micro)
                ) {
                    Text(
                        text = settings.hardwareId,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hardware Bay Compartment Graphic (4 bays)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.indicatorDot)
            ) {
                settings.bays.forEach { bay ->
                    val isTaken = bay.isTaken
                    val bayBg = if (isTaken) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    }
                    val bayContentColor = if (isTaken) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ControlShape)
                            .background(bayBg)
                            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape)
                            .padding(vertical = spacing.small),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = bay.bayId,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTaken) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bay.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = bayContentColor,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isTaken) Icons.Default.CheckCircle else if (bay.label == "EVE") Icons.Default.Schedule else Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = if (isTaken) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(spacing.medium)
                                    .padding(top = spacing.micro)
                            )
                        }
                    }
                }
            }

            // Telemetry strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                    .padding(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(spacing.iconMedium)
                    )
                    Text(
                        text = "Last sync ${settings.lastSyncTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Battery5Bar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(spacing.iconMedium)
                    )
                    Text(
                        text = "${settings.batteryPercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onPairNewBay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.huge),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.iconLarge)
                    )
                    Text(
                        text = "Pair New Apothecary Bay",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareNetworkSection(settings: DeviceSettings, onSelectSnooze: (Int) -> Unit, onTestTone: () -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HARDWARE & NETWORK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.WifiTethering,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconMedium)
                )
            }

            // Wi-Fi Network Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(spacing.iconExtraLarge)
                    )
                    Column {
                        Text(
                            text = "CONNECTED NETWORK",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = settings.wifiSsid,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconLarge)
                )
            }

            // Sound Volume & Tone Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(spacing.iconExtraLarge)
                    )
                    Column {
                        Text(
                            text = "SOUND & CHIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = settings.soundChimeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = onTestTone,
                    modifier = Modifier
                        .size(spacing.controlSize)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Test tone chime",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(spacing.iconLarge)
                    )
                }
            }

            // Snooze Interval Segmented Switcher
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "REMINDER SNOOZE INTERVAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.snoozeIntervalMinutes} min selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    listOf(5, 10, 15).forEach { minutes ->
                        val isSelected = minutes == settings.snoozeIntervalMinutes
                        val btnBg = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        }
                        val btnTextColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.controlSize)
                                .clip(ControlShape)
                                .background(btnBg)
                                .clickable { onSelectSnooze(minutes) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "$minutes minutes snooze"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$minutes min",
                                style = MaterialTheme.typography.bodyMedium,
                                color = btnTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareCircleSection(settings: DeviceSettings) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CARE CIRCLE & TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconMedium)
                )
            }

            settings.caregivers.forEach { caregiver ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(spacing.medium)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(spacing.controlSize)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = caregiver.initials,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(
                                        text = caregiver.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = caregiver.relationship.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = spacing.small, vertical = spacing.micro)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(spacing.indicatorDot)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary)
                                    )
                                    Text(
                                        text = "ALERTS ON",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(spacing.iconMedium)
                                )
                                Text(
                                    text = caregiver.phoneNumber,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .size(spacing.controlSize)
                                    .clip(ControlShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call caregiver",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(spacing.iconLarge)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.huge),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.iconLarge)
                    )
                    Text(
                        text = "+ Add caregiver contact",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RefillThresholdSection(currentThreshold: Int, onThresholdChange: (Int) -> Unit) {
    val spacing = LocalSpacing.current
    var sliderVal by remember(currentThreshold) { mutableFloatStateOf(currentThreshold.toFloat()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REFILL & INVENTORY THRESHOLDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconMedium)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(spacing.medium)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Low-stock Alert Trigger",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = spacing.medium, vertical = spacing.micro)
                        ) {
                            Text(
                                text = "${sliderVal.toInt()} tablets",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "DoseGuard prompts refill notifications to your pharmacy and caregiver when individual bay supply dips below threshold.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = sliderVal,
                        onValueChange = {
                            sliderVal = it
                            onThresholdChange(it.toInt())
                        },
                        valueRange = 1f..15f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceDim
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1 TAB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "5 TABS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "10 TABS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "15 TABS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualEnvironmentSection(isDark: Boolean, onToggleTheme: (Boolean) -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VISUAL ENVIRONMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconMedium)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Appearance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tonal Paper & Contrast",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    val boneBg = if (!isDark) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                    val boneTextColor = if (!isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .height(spacing.avatarSize)
                            .clip(PillShape)
                            .background(boneBg)
                            .clickable { onToggleTheme(false) }
                            .padding(horizontal = spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = boneTextColor,
                                modifier = Modifier.size(spacing.medium)
                            )
                            Text(
                                text = "Bone",
                                style = MaterialTheme.typography.bodyMedium,
                                color = boneTextColor,
                                fontWeight = if (!isDark) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    val charBg = if (isDark) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                    val charTextColor = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .height(spacing.avatarSize)
                            .clip(PillShape)
                            .background(charBg)
                            .clickable { onToggleTheme(true) }
                            .padding(horizontal = spacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = charTextColor,
                                modifier = Modifier.size(spacing.medium)
                            )
                            Text(
                                text = "Char",
                                style = MaterialTheme.typography.bodyMedium,
                                color = charTextColor,
                                fontWeight = if (isDark) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemInfoSection(settings: DeviceSettings, onSignOut: () -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.large)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.avatarSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(spacing.iconLarge)
                )
            }

            Text(
                text = "DoseGuard Apothecary OS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Firmware ${settings.firmwareVersion} • Hardware Serial ${settings.serialNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.material3.OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small)
                    .height(spacing.controlSize),
                shape = PillShape,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = "Sign Out of Account",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Row(
                modifier = Modifier.padding(top = spacing.small),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIVACY TELEMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "REGULATORY COMPLIANCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    text = "SAFETY GUIDE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ClinicalProtocolsSection(
    settings: DeviceSettings,
    onSelectUnitSystem: (String) -> Unit,
    onSelectGracePeriod: (Int) -> Unit,
    onSelectReminderRepeat: (Int) -> Unit,
    onToggleQuietHours: (Boolean) -> Unit
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_section_dispenser),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.large)
                )
            }

            // Measurement Units
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = stringResource(R.string.settings_units_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    val metricLabel = stringResource(R.string.settings_units_metric)
                    val imperialLabel = stringResource(R.string.settings_units_imperial)
                    val units = listOf(metricLabel, imperialLabel)
                    units.forEach { unit ->
                        val isSelected = (unit == metricLabel && settings.unitSystem.startsWith("Metric")) ||
                            (unit == imperialLabel && settings.unitSystem.startsWith("Imperial"))
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.minTouchTarget)
                                .clip(ControlShape)
                                .background(bg)
                                .clickable { onSelectUnitSystem(unit) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = unit
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Grace Period
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_grace_period_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.gracePeriodMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val isSelected = mins == settings.gracePeriodMinutes
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.minTouchTarget)
                                .clip(ControlShape)
                                .background(bg)
                                .clickable { onSelectGracePeriod(mins) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "$mins minutes grace period"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$mins m",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Max Reminder Repeats
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminder_repeats_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.reminderRepeatCount}x repeats",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(spacing.extraSmall),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    listOf(1, 2, 3, 5).forEach { count ->
                        val isSelected = count == settings.reminderRepeatCount
                        val bg = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.minTouchTarget)
                                .clip(ControlShape)
                                .background(bg)
                                .clickable { onSelectReminderRepeat(count) }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "$count repeats"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${count}x",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Quiet Hours
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onToggleQuietHours(!settings.quietHoursEnabled) }
                    .padding(spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = if (settings.quietHoursEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(spacing.large)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.settings_quiet_hours_label),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = settings.quietHoursText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                androidx.compose.material3.Switch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = { onToggleQuietHours(it) }
                )
            }
        }
    }
}

@Composable
private fun ClinicalExportSection(onExportCsv: () -> Unit, onExportSummary: () -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CLINICAL AUDIT & EXPORT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.large)
                )
            }

            Text(
                text = "Export verified telemetry and dispenser event records for clinician consultations, pharmacy reconciliation, or personal archive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onExportCsv,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.large)
                    )
                    Text(
                        text = stringResource(R.string.settings_export_csv),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onExportSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.large)
                    )
                    Text(
                        text = stringResource(R.string.settings_export_summary),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyAndDataSection(onNavigateToPrivacy: () -> Unit, onClearAllData: () -> Unit) {
    val spacing = LocalSpacing.current
    var showDeleteConfirmation by remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showDeleteConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_delete_data),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_delete_data_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onClearAllData()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = PillShape
                ) {
                    Text(text = "Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(text = "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_section_privacy),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.large)
                )
            }

            Button(
                onClick = onNavigateToPrivacy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.large)
                    )
                    Text(
                        text = stringResource(R.string.settings_privacy_policy),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            androidx.compose.material3.OutlinedButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight),
                shape = ControlShape,
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.material3.ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.large)
                    )
                    Text(
                        text = stringResource(R.string.settings_delete_data),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
