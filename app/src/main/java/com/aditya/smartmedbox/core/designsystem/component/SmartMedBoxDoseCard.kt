package com.aditya.smartmedbox.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@Composable
fun SmartMedBoxDoseCard(
    medicineName: String,
    dosage: String,
    timeOrCountdown: String,
    trayLabel: String,
    modifier: Modifier = Modifier,
    status: DoseStatus? = null,
    progressFraction: Float? = null,
    progressSubtext: Pair<String, String>? = null,
    latchStatusText: String? = null,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = clickableModifier
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = spacing.hairlineBorder,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CardShape
            )
            .padding(spacing.medium)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            // Header: Section label & Tray Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEXT DOSE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    modifier = Modifier
                        .clip(PillShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(
                            width = spacing.hairlineBorder,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = PillShape
                        )
                        .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(spacing.medium)
                    )
                    Text(
                        text = trayLabel.uppercase(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Hero Metric & Medication Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeOrCountdown,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displayMedium
                    )
                    Text(
                        text = "$medicineName • $dosage",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = spacing.extraSmall)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(spacing.huge)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .border(
                            width = spacing.hairlineBorder,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = ControlShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Medication,
                        contentDescription = medicineName,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(spacing.large)
                    )
                }
            }

            // Timeline Progress bar (if provided)
            if (progressFraction != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(spacing.extraSmall)
                            .clip(PillShape)
                    )

                    if (progressSubtext != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = progressSubtext.first.uppercase(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = progressSubtext.second.uppercase(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Bottom action row or status chip / Quick Action Tray Trigger
            val hasAction = actionButtonText != null && onActionClick != null
            val hasLatch = latchStatusText != null
            val hasBottomRow = status != null || hasAction || hasLatch
            if (hasBottomRow) {
                if (hasAction || hasLatch) {
                    HorizontalDivider(
                        thickness = spacing.hairlineBorder,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (status != null) {
                        SmartMedBoxStatusChip(status = status)
                    } else if (latchStatusText != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(spacing.iconMedium)
                            )
                            Text(
                                text = latchStatusText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (actionButtonText != null && onActionClick != null) {
                        SmartMedBoxSecondaryButton(
                            text = actionButtonText,
                            onClick = onActionClick
                        )
                    }
                }
            }
        }
    }
}
