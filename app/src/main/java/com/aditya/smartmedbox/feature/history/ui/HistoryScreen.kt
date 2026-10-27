package com.aditya.smartmedbox.feature.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxIntakeLogRow
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxStatusChip
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.feature.history.domain.model.DailyAdherence

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onEvent: (HistoryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "History",
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
                            contentDescription = "User Profile",
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
                is HistoryUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Loading adherence records and box sensor logs...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HistoryUiState.Empty -> {
                    SmartMedBoxEmptyState(
                        title = "No Intake Records Yet",
                        message = "Prescriptions and dispensing history will appear here once tracked.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HistoryUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Failed to Load History",
                        message = uiState.message,
                        onRetry = { onEvent(HistoryUiEvent.Retry) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }
                is HistoryUiState.Offline -> {
                    SmartMedBoxErrorState(
                        title = "History Offline",
                        message = "Unable to synchronize latest adherence data. Showing cached ledger.",
                        onRetry = { onEvent(HistoryUiEvent.Refresh) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }
                is HistoryUiState.Content -> {
                    HistoryContent(
                        state = uiState,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState.Content,
    onEvent: (HistoryUiEvent) -> Unit
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

        // 1. SCREEN HEADER
        Column {
            Text(
                text = "ADHERENCE & HISTORY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Past 30 Days",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 2. TOP METRIC BANNER (OVERALL ADHERENCE + STREAK)
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
                    Column {
                        Text(
                            text = "${state.stats.overallAdherenceRatePercent}%",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Overall adherence rate this month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    SmartMedBoxStatusChip(
                        status = DoseStatus.ON_TIME,
                        customLabel = "TARGET > 90% ACHIEVED"
                    )
                }

                // Divider hairline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.hairlineBorder)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Streak Subcard
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.huge)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(spacing.large)
                            )
                        }

                        Column {
                            Text(
                                text = "${state.stats.streakDays}-day streak",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.stats.streakSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Active Streak",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(spacing.medium)
                        )
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 3. WEEKLY BREAKDOWN BAR CHART
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WEEKLY BREAKDOWN (TAKEN VS MISSED)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "WEEK 42",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Stacked Bar Columns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .padding(top = spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    state.stats.weeklyBreakdown.forEach { day ->
                        WeeklyBarColumn(day = day)
                    }
                }

                // Baseline hairline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.hairlineBorder)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = spacing.extraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(spacing.small)
                                    .background(MaterialTheme.colorScheme.secondary, ControlShape)
                            )
                            Text(
                                text = "Taken (${state.stats.totalTaken})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(spacing.small)
                                    .background(MaterialTheme.colorScheme.error, ControlShape)
                            )
                            Text(
                                text = "Missed (${state.stats.totalMissed})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "92.8% WK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. ADHERENCE LEDGER WITH FILTER CHIPS
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ADHERENCE LEDGER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Filter by status",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Filter Chips (ALL / TAKEN / MISSED)
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                HistoryFilterChip(
                    label = "ALL",
                    isSelected = state.currentFilter == HistoryFilter.ALL,
                    onClick = { onEvent(HistoryUiEvent.SetFilter(HistoryFilter.ALL)) }
                )
                HistoryFilterChip(
                    label = "TAKEN",
                    isSelected = state.currentFilter == HistoryFilter.TAKEN,
                    onClick = { onEvent(HistoryUiEvent.SetFilter(HistoryFilter.TAKEN)) }
                )
                HistoryFilterChip(
                    label = "MISSED",
                    isSelected = state.currentFilter == HistoryFilter.MISSED,
                    onClick = { onEvent(HistoryUiEvent.SetFilter(HistoryFilter.MISSED)) }
                )
            }
        }

        // 5. GROUPED INTAKE LOG ENTRIES
        val groupedLogs = state.filteredLogs.groupBy { it.dateGroup }
        groupedLogs.forEach { (dateHeader, logsInGroup) ->
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = dateHeader.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.small, start = spacing.extraSmall)
                )

                logsInGroup.forEach { logItem ->
                    SmartMedBoxIntakeLogRow(
                        medicineName = logItem.medicineName,
                        dosageAndInstructions = logItem.dosageInstructions,
                        timeAndTray = logItem.timeAndBay,
                        status = logItem.status
                    )
                }
            }
        }

        // 6. CAREGIVER LEDGER SEALED FOOTER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LockReset,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.large)
                )
                Text(
                    text = "TELEMETRY LEDGER SEALED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "BOX ID #DG-8820",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.medium))
    }
}

@Composable
private fun WeeklyBarColumn(day: DailyAdherence) {
    val spacing = LocalSpacing.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        modifier = Modifier.width(36.dp)
    ) {
        Column(
            modifier = Modifier
                .width(16.dp)
                .height(80.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            if (day.missedCount > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((day.takenCount * 18).dp.coerceAtMost(62.dp))
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }

        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .height(spacing.chipHeight)
            .clip(PillShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
