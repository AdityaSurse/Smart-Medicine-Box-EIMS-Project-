package com.aditya.smartmedbox.core.designsystem.gallery

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxDoseCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxIntakeLogRow
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxMetricCard
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxPrimaryButton
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxSecondaryButton
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxStatusChip
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxUrgentButton
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme

/**
 * Debug-only gallery screen showcasing every Stitch design system component
 * in both Light (Bone & Paper) and Dark (Char & Soot) environments.
 */
@Composable
fun DesignSystemGalleryScreen() {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "Design System Gallery",
                actions = {
                    IconButton(onClick = {}) {
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
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(spacing.medium)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(spacing.large)
        ) {
            item { Spacer(modifier = Modifier.height(spacing.extraSmall)) }

            // 1. TYPOGRAPHY & BRAND VOICE
            item {
                SectionHeader(title = "Typography Scale")
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(spacing.medium)
                ) {
                    Text(
                        text = "10h 30m",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Editorial Newsreader Serif",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Clean Humanist DM Sans Body text crafted with a 16px floor for optimal senior legibility.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "BAY 02 // MORNING PROTOCOL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. STATUS CHIPS (SEMANTIC BADGES)
            item {
                SectionHeader(title = "Status Chips (Semantic Dose States)")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    SmartMedBoxStatusChip(status = DoseStatus.TAKEN)
                    SmartMedBoxStatusChip(status = DoseStatus.PENDING)
                    SmartMedBoxStatusChip(status = DoseStatus.MISSED)
                    SmartMedBoxStatusChip(status = DoseStatus.ON_TIME)
                }
            }

            // 3. METRIC CARDS (BENTO 2X2 GRID)
            item {
                SectionHeader(title = "Metric Cards (Telemetry & Status)")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        SmartMedBoxMetricCard(
                            label = "Internal Temp",
                            value = "28.4°C",
                            icon = Icons.Outlined.DeviceThermostat,
                            subtext = "Optimal",
                            modifier = Modifier.weight(1f)
                        )
                        SmartMedBoxMetricCard(
                            label = "Humidity",
                            value = "55%",
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
                            value = "18",
                            icon = Icons.Outlined.Inventory2,
                            subtext = "tablets",
                            modifier = Modifier.weight(1f)
                        )
                        SmartMedBoxMetricCard(
                            label = "Cadence",
                            value = "On time",
                            icon = Icons.Outlined.Verified,
                            statusIndicator = {
                                SmartMedBoxStatusChip(status = DoseStatus.ON_TIME)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. HERO NEXT DOSE CARD
            item {
                SectionHeader(title = "Dose Card (Hero Next Dose)")
                SmartMedBoxDoseCard(
                    medicineName = "Amoxicillin 500mg",
                    dosage = "1 tablet • 9:00 PM",
                    timeOrCountdown = "10h 30m",
                    trayLabel = "Tray 02",
                    progressFraction = 0.68f,
                    progressSubtext = "Last taken 1:30 PM" to "Due 9:00 PM",
                    actionButtonText = "Dispense Now",
                    onActionClick = {}
                )
            }

            // 5. INTAKE LOG ROWS (TODAY'S SCHEDULE LEDGER)
            item {
                SectionHeader(title = "Intake-Log Rows (Ledger Entries)")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    SmartMedBoxIntakeLogRow(
                        medicineName = "Levothyroxine 75mcg",
                        dosageAndInstructions = "1 tablet • Fasted",
                        timeAndTray = "8:00 AM • Tray 01",
                        status = DoseStatus.TAKEN
                    )
                    SmartMedBoxIntakeLogRow(
                        medicineName = "Vitamin D3 2000 IU",
                        dosageAndInstructions = "1 softgel • With lunch",
                        timeAndTray = "1:30 PM • Tray 02",
                        status = DoseStatus.PENDING
                    )
                    SmartMedBoxIntakeLogRow(
                        medicineName = "Omega-3 1000mg",
                        dosageAndInstructions = "1 capsule • Dinner",
                        timeAndTray = "6:00 PM • Tray 03",
                        status = DoseStatus.MISSED
                    )
                }
            }

            // 6. BUTTON CONTROLS
            item {
                SectionHeader(title = "Button Controls")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                    SmartMedBoxPrimaryButton(
                        text = "Early Dispense Bay 04",
                        onClick = {},
                        leadingIcon = Icons.Outlined.LockOpen
                    )
                    SmartMedBoxSecondaryButton(
                        text = "Pair New Apothecary Bay",
                        onClick = {},
                        leadingIcon = Icons.Outlined.Inventory2,
                        fullWidth = true
                    )
                    SmartMedBoxUrgentButton(
                        text = "Acknowledge Missed Dose",
                        onClick = {},
                        leadingIcon = Icons.Outlined.Medication
                    )
                }
            }

            // 7. EMPTY / ERROR / LOADING STATES
            item {
                SectionHeader(title = "States (Loading, Empty, Error)")
                Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    SmartMedBoxLoadingState(message = "Synchronizing with Smart Dispenser #DG-4081...")

                    SmartMedBoxEmptyState(
                        title = "No Doses Scheduled",
                        message = "All prescriptions for today have been completed or none are logged.",
                        actionButtonText = "Add Medicine",
                        onActionClick = {}
                    )

                    SmartMedBoxErrorState(
                        title = "Hardware Sensor Timeout",
                        message = "Could not establish BLE connection with Smart Box. Please verify battery power.",
                        onRetry = {}
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(spacing.huge)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val spacing = LocalSpacing.current
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = spacing.small, top = spacing.extraSmall)
    )
}

// ============================================================================
// LIGHT & DARK PREVIEWS
// ============================================================================

@Preview(
    name = "Design System Gallery - Light (Bone/Paper)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun DesignSystemGalleryScreenLightPreview() {
    SmartMedBoxTheme(darkTheme = false) {
        Surface {
            DesignSystemGalleryScreen()
        }
    }
}

@Preview(
    name = "Design System Gallery - Dark (Char/Soot)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun DesignSystemGalleryScreenDarkPreview() {
    SmartMedBoxTheme(darkTheme = true) {
        Surface {
            DesignSystemGalleryScreen()
        }
    }
}
