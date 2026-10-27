package com.aditya.smartmedbox.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.semanticColors

@Composable
fun SmartMedBoxIntakeLogRow(
    medicineName: String,
    dosageAndInstructions: String,
    timeAndTray: String,
    status: DoseStatus,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current
    val semanticColors = MaterialTheme.semanticColors

    val token = when (status) {
        DoseStatus.TAKEN -> semanticColors.taken
        DoseStatus.PENDING -> semanticColors.pending
        DoseStatus.MISSED -> semanticColors.missed
        DoseStatus.ON_TIME -> semanticColors.onTime
    }

    val iconVector = when (status) {
        DoseStatus.TAKEN -> Icons.Outlined.Check
        DoseStatus.PENDING -> Icons.Outlined.Schedule
        DoseStatus.MISSED -> Icons.Outlined.Close
        DoseStatus.ON_TIME -> Icons.Outlined.Check
    }

    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = spacing.huge)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = spacing.hairlineBorder,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CardShape
            )
            .padding(spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading Section: Icon avatar + Medication details
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            modifier = Modifier.weight(1f).padding(end = spacing.small)
        ) {
            Box(
                modifier = Modifier
                    .size(spacing.huge)
                    .clip(CircleShape)
                    .background(token.container)
                    .border(
                        width = spacing.hairlineBorder,
                        color = token.border,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = token.content,
                    modifier = Modifier.size(spacing.large)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Text(
                    text = timeAndTray.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = medicineName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dosageAndInstructions,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing Status Chip
        SmartMedBoxStatusChip(
            status = status
        )
    }
}
