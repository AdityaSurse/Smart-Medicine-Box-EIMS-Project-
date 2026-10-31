package com.aditya.smartmedbox.feature.schedule.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay
import kotlinx.coroutines.delay

@Composable
fun ScheduleScreen(uiState: ScheduleUiState, onEvent: (ScheduleUiEvent) -> Unit, modifier: Modifier = Modifier, onNavigateToAlerts: () -> Unit = {}) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "Schedule",
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
                is ScheduleUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Loading prescription ledger and schedules...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ScheduleUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Schedule Synchronization Error",
                        message = uiState.message,
                        onRetry = { onEvent(ScheduleUiEvent.Refresh) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }

                is ScheduleUiState.Offline -> {
                    ScheduleContent(
                        days = uiState.days,
                        selectedDay = 16,
                        schedules = uiState.schedules,
                        isTraySynced = false,
                        isSavingSchedule = false,
                        feedbackMessage = null,
                        onEvent = onEvent
                    )
                }

                is ScheduleUiState.Empty -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WeeklyLedgerHeader(
                            days = uiState.days,
                            selectedDay = uiState.selectedDayNumber,
                            isTraySynced = true,
                            onSelectDay = { onEvent(ScheduleUiEvent.SelectDay(it)) }
                        )
                        SmartMedBoxEmptyState(
                            title = "No Regimens Scheduled",
                            message = uiState.message,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is ScheduleUiState.Success -> {
                    ScheduleContent(
                        days = uiState.days,
                        selectedDay = uiState.selectedDayNumber,
                        schedules = uiState.schedules,
                        isTraySynced = uiState.isTraySynced,
                        isSavingSchedule = uiState.isSavingSchedule,
                        feedbackMessage = uiState.userFeedbackMessage,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleContent(
    days: List<ScheduleDay>,
    selectedDay: Int,
    schedules: List<PrescriptionSchedule>,
    isTraySynced: Boolean,
    isSavingSchedule: Boolean,
    feedbackMessage: String?,
    onEvent: (ScheduleUiEvent) -> Unit
) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = spacing.marginMobile, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        item {
            WeeklyLedgerHeader(
                days = days,
                selectedDay = selectedDay,
                isTraySynced = isTraySynced,
                onSelectDay = { onEvent(ScheduleUiEvent.SelectDay(it)) }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE FOR WEDNESDAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${schedules.size} COURSES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(schedules, key = { it.id }) { schedule ->
            ScheduleMedicationCard(
                schedule = schedule,
                onToggleActive = { isActive ->
                    onEvent(ScheduleUiEvent.ToggleActive(schedule.id, isActive))
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "APOTHECARY LEDGER ENTRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = spacing.medium)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }

        item {
            NewRegimenFormCard(
                isSaving = isSavingSchedule,
                feedbackMessage = feedbackMessage,
                onSave = { name, strength, bay, times, repeatDays, supply, formFactor ->
                    onEvent(
                        ScheduleUiEvent.SaveSchedule(
                            medicineName = name,
                            dosageStrength = strength,
                            compartmentBay = bay,
                            dispenseTimes = times,
                            repeatDays = repeatDays,
                            totalSupply = supply,
                            formFactor = formFactor
                        )
                    )
                },
                onClearFeedback = { onEvent(ScheduleUiEvent.ClearFeedback) }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(spacing.iconSmall)
                )
                Spacer(modifier = Modifier.width(spacing.small))
                Text(
                    text = "DEVICE SYNCHRONIZATION ENCRYPTED & VERIFIED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeeklyLedgerHeader(days: List<ScheduleDay>, selectedDay: Int, isTraySynced: Boolean, onSelectDay: (Int) -> Unit) {
    val spacing = LocalSpacing.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WEEKLY LEDGER",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Schedule",
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
                    .padding(horizontal = spacing.medium, vertical = spacing.extraSmall),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.small)
                            .clip(CircleShape)
                            .background(
                                if (isTraySynced) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                    )
                    Text(
                        text = if (isTraySynced) "TRAY SYNCED" else "OFFLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isTraySynced) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        // 7-Day Horizontal Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.indicatorDot)
        ) {
            days.forEach { day ->
                val isSelected = day.dayNumber == selectedDay
                val bg = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
                val textDayColor = if (isSelected) {
                    MaterialTheme.colorScheme.inversePrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                val textNumColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CardShape)
                        .background(bg)
                        .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                        .clickable { onSelectDay(day.dayNumber) }
                        .padding(vertical = spacing.small)
                        .semantics {
                            role = Role.Button
                            contentDescription = "${day.dayLetter} the ${day.dayNumber}, ${if (isSelected) "Selected" else "Not selected"}"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day.dayLetter,
                            style = MaterialTheme.typography.labelSmall,
                            color = textDayColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(spacing.micro))
                        Text(
                            text = "${day.dayNumber}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = textNumColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleMedicationCard(schedule: PrescriptionSchedule, onToggleActive: (Boolean) -> Unit) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.large)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.avatarSize)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(spacing.iconLarge)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            Text(
                                text = schedule.medicineName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(ControlShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(horizontal = spacing.small, vertical = spacing.micro)
                            ) {
                                Text(
                                    text = schedule.trayBay.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(spacing.micro))
                        Text(
                            text = "${schedule.dosageStrength} • ${schedule.formFactor}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = schedule.isActive,
                    onCheckedChange = onToggleActive,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "Toggle ${schedule.medicineName} schedule status"
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    schedule.dispenseTimes.forEach { time ->
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
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(spacing.iconSmall)
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

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
                        text = schedule.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NewRegimenFormCard(
    isSaving: Boolean,
    feedbackMessage: String?,
    onSave: (String, String, String, List<String>, List<String>, Int, String) -> Unit,
    onClearFeedback: () -> Unit
) {
    val spacing = LocalSpacing.current

    var medicineName by remember { mutableStateOf("Atorvastatin") }
    var dosageMg by remember { mutableIntStateOf(20) }
    var selectedBay by remember { mutableStateOf("Bay 02") }
    val timesList = remember { mutableStateListOf("08:00 PM") }
    val repeatDays = remember { mutableStateListOf("M", "T", "W", "T", "F") }
    var totalSupply by remember { mutableStateOf("30") }
    var formFactor by remember { mutableStateOf("Coated Pill") }

    val allDays = listOf("M", "T", "W", "T", "F", "S", "S")
    val allBays = listOf("Bay 01", "Bay 02", "Bay 03", "Bay 04")

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2500)
            onClearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.large)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "NEW REGIMEN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Configure Dosage",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .size(spacing.barColumnWidth)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(spacing.iconLarge)
                    )
                }
            }

            // Medicine Name Input
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = "MEDICINE NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    placeholder = { Text("e.g. Lisinopril") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = ControlShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )
            }

            // Dosage Strength Stepper
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = "DOSAGE STRENGTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.inputHeight)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape)
                        .padding(horizontal = spacing.extraSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (dosageMg > 5) dosageMg -= 5 },
                        modifier = Modifier
                            .size(spacing.controlSize)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease dosage",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "$dosageMg mg",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { if (dosageMg < 1000) dosageMg += 5 },
                        modifier = Modifier
                            .size(spacing.controlSize)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase dosage",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Hardware Compartment Bay Selector
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDWARE COMPARTMENT BAY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$selectedBay READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    allBays.forEach { bay ->
                        val isBaySelected = bay == selectedBay
                        val bayBg = if (isBaySelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                        val bayTextColor = if (isBaySelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                        val baySubColor = if (isBaySelected) {
                            MaterialTheme.colorScheme.inversePrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.buttonHeight)
                                .clip(ControlShape)
                                .background(bayBg)
                                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape)
                                .clickable { selectedBay = bay }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "$bay, ${if (isBaySelected) "Selected" else "Not selected"}"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "BAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = baySubColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = bay.takeLast(2),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = bayTextColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Dispense Times
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = "DISPENSE TIMES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    timesList.forEachIndexed { index, time ->
                        Box(
                            modifier = Modifier
                                .clip(ControlShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape)
                                .padding(horizontal = spacing.medium, vertical = spacing.small)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(spacing.small)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(spacing.iconMedium)
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                if (timesList.size > 1) {
                                    IconButton(
                                        onClick = { timesList.removeAt(index) },
                                        modifier = Modifier.size(spacing.iconLarge)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove time $time",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(spacing.iconSmall)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "+ Add another time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable {
                            if (timesList.size < 4) {
                                val nextTime = if (timesList.size == 1) "08:00 AM" else "12:00 PM"
                                timesList.add(nextTime)
                            }
                        }
                        .padding(vertical = spacing.extraSmall)
                )
            }

            // Recurrence Cycle
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECURRENCE CYCLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DAILY SCHEDULE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    allDays.forEach { dayLetter ->
                        val isDaySelected = repeatDays.contains(dayLetter)
                        val dayBg = if (isDaySelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        }
                        val dayTextColor = if (isDaySelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(dayBg)
                                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable {
                                    if (isDaySelected) {
                                        if (repeatDays.size > 1) repeatDays.remove(dayLetter)
                                    } else {
                                        repeatDays.add(dayLetter)
                                    }
                                }
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Day $dayLetter, ${if (isDaySelected) "Selected" else "Not selected"}"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLetter,
                                style = MaterialTheme.typography.bodyMedium,
                                color = dayTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Total Supply & Form Factor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Text(
                        text = "TOTAL SUPPLY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = totalSupply,
                        onValueChange = { totalSupply = it.filter { ch -> ch.isDigit() } },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ControlShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                ) {
                    Text(
                        text = "FORM FACTOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = formFactor,
                        onValueChange = { formFactor = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = ControlShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        singleLine = true
                    )
                }
            }

            // Save Schedule CTA
            Button(
                onClick = {
                    val count = totalSupply.toIntOrNull() ?: 30
                    onSave(
                        medicineName,
                        "$dosageMg mg",
                        selectedBay,
                        timesList.toList(),
                        repeatDays.toList(),
                        count,
                        formFactor
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.buttonHeight),
                shape = ControlShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isSaving
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    if (feedbackMessage != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondaryContainer
                        )
                        Text(
                            text = feedbackMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null
                        )
                        Text(
                            text = if (isSaving) "Arming Bay..." else "Save schedule",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
