package com.aditya.smartmedbox.feature.medicines.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aditya.smartmedbox.R
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.core.designsystem.theme.SmartMedBoxTheme
import com.aditya.smartmedbox.feature.medicines.domain.model.Medicine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesScreen(
    uiState: MedicinesUiState,
    onEvent: (MedicinesUiEvent) -> Unit,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToEditMedicine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "Medication Inventory",
                actions = {
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.padding(end = spacing.medium)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = spacing.small, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                            Spacer(modifier = Modifier.width(spacing.extraSmall))
                            Text(
                                text = "Bays Armed",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .semantics { contentDescription = "Add new medication schedule" }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = spacing.medium)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(spacing.extraSmall))
                    Text(
                        text = "Add Medicine",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is MedicinesUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Loading medication inventory...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is MedicinesUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Unable to load inventory",
                        message = uiState.message,
                        onRetry = { onEvent(MedicinesUiEvent.Refresh) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is MedicinesUiState.Empty -> {
                    if (uiState.isSearchResult) {
                        SmartMedBoxEmptyState(
                            title = "No medications found",
                            message = "Try adjusting your search terms or filter.",
                            actionButtonText = "Clear filters",
                            onActionClick = {
                                onEvent(MedicinesUiEvent.Search(""))
                                onEvent(MedicinesUiEvent.ToggleFilterLowStock)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        SmartMedBoxEmptyState(
                            title = "No medications scheduled",
                            message = "Add your first prescription to allocate it to a smart compartment bay.",
                            actionButtonText = "Add Medicine",
                            onActionClick = onNavigateToAddMedicine,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                is MedicinesUiState.Success -> {
                    MedicinesContent(
                        state = uiState,
                        onEvent = onEvent,
                        onEditMedicine = onNavigateToEditMedicine
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicinesContent(state: MedicinesUiState.Success, onEvent: (MedicinesUiEvent) -> Unit, onEditMedicine: (String) -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val lowStockCount = state.medicines.count { it.currentStock <= 5 }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.marginMobile,
            end = spacing.marginMobile,
            top = spacing.small,
            bottom = 88.dp // Space for FAB
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        // Search & Filter Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(MedicinesUiEvent.Search(it)) },
                    placeholder = {
                        Text(
                            text = "Search medication, dosage, or bay...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(MedicinesUiEvent.Search("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = state.filterLowStock,
                        onClick = { onEvent(MedicinesUiEvent.ToggleFilterLowStock) },
                        label = {
                            Text(
                                text = "Low Stock Only ($lowStockCount)",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            if (state.filterLowStock) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        shape = PillShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )

                    Text(
                        text = "${state.medicines.size} Regimens Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // List of Medicine Cards
        items(
            items = state.filteredMedicines,
            key = { it.id }
        ) { medicine ->
            MedicineCard(
                medicine = medicine,
                onToggleActive = { onEvent(MedicinesUiEvent.ToggleActive(medicine.id)) },
                onEdit = { onEditMedicine(medicine.id) },
                onDelete = { onEvent(MedicinesUiEvent.DeleteMedicine(medicine.id)) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MedicineCard(medicine: Medicine, onToggleActive: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val isLowStock = medicine.currentStock <= 5

    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            // Top Row: Icon + Name + Bay Pill + Active Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactile pill container
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconRes = if (medicine.formFactor.contains("Capsule", ignoreCase = true)) {
                            R.drawable.ic_pill
                        } else {
                            R.drawable.ic_tablet
                        }
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                        ) {
                            Text(
                                text = medicine.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = medicine.compartmentLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = medicine.dosageDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active toggle switch
                Switch(
                    checked = medicine.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.semantics {
                        contentDescription = "Toggle ${medicine.name} dispensing schedule"
                    }
                )
            }

            // Middle Row: Inventory remaining indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isLowStock) {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isLowStock) {
                            MaterialTheme.colorScheme.error.copy(
                                alpha = 0.3f
                            )
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = spacing.small, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                            imageVector = if (isLowStock) Icons.Default.WarningAmber else Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isLowStock) "LOW SUPPLY ALERT" else "SUPPLY REMAINING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${medicine.currentStock} / ${medicine.totalStock} ${medicine.formFactor}s",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                LinearProgressIndicator(
                    progress = { (medicine.currentStock.toFloat() / medicine.totalStock.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(PillShape),
                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Bottom Row: Timings badges + Actions (Edit & Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    modifier = Modifier.weight(1f)
                ) {
                    medicine.timings.forEach { time ->
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit ${medicine.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete ${medicine.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Medicines List - Light")
@Composable
private fun MedicinesScreenPreview() {
    SmartMedBoxTheme(darkTheme = false) {
        MedicinesScreen(
            uiState = MedicinesUiState.Success(
                medicines = listOf(
                    Medicine(
                        id = "1",
                        name = "Amoxicillin",
                        dosageDescription = "500 mg • Antibiotic capsule",
                        compartmentLabel = "Bay 01",
                        currentStock = 18,
                        totalStock = 30
                    ),
                    Medicine(
                        id = "2",
                        name = "Levothyroxine",
                        dosageDescription = "75 mcg • Thyroid hormone",
                        compartmentLabel = "Bay 03",
                        currentStock = 4,
                        totalStock = 30
                    )
                ),
                filteredMedicines = listOf(
                    Medicine(
                        id = "1",
                        name = "Amoxicillin",
                        dosageDescription = "500 mg • Antibiotic capsule",
                        compartmentLabel = "Bay 01",
                        currentStock = 18,
                        totalStock = 30
                    ),
                    Medicine(
                        id = "2",
                        name = "Levothyroxine",
                        dosageDescription = "75 mcg • Thyroid hormone",
                        compartmentLabel = "Bay 03",
                        currentStock = 4,
                        totalStock = 30
                    )
                )
            ),
            onEvent = {},
            onNavigateToAddMedicine = {},
            onNavigateToEditMedicine = {}
        )
    }
}
