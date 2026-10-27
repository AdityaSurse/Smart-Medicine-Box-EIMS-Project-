package com.aditya.smartmedbox.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.core.designsystem.theme.semanticColors

enum class DoseStatus {
    TAKEN,
    PENDING,
    MISSED,
    ON_TIME
}

@Composable
fun SmartMedBoxStatusChip(
    status: DoseStatus,
    modifier: Modifier = Modifier,
    customLabel: String? = null
) {
    val spacing = LocalSpacing.current
    val semanticColors = MaterialTheme.semanticColors

    val token = when (status) {
        DoseStatus.TAKEN -> semanticColors.taken
        DoseStatus.PENDING -> semanticColors.pending
        DoseStatus.MISSED -> semanticColors.missed
        DoseStatus.ON_TIME -> semanticColors.onTime
    }

    val labelText = customLabel ?: when (status) {
        DoseStatus.TAKEN -> "TAKEN"
        DoseStatus.PENDING -> "PENDING"
        DoseStatus.MISSED -> "MISSED"
        DoseStatus.ON_TIME -> "ON TIME"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
        modifier = modifier
            .height(spacing.chipHeight)
            .clip(PillShape)
            .background(token.container)
            .border(
                width = spacing.hairlineBorder,
                color = token.border,
                shape = PillShape
            )
            .padding(horizontal = spacing.gutterMobile)
    ) {
        // 8dp circular status pip echoing Stitch specifications
        Box(
            modifier = Modifier
                .size(spacing.small)
                .background(color = token.content, shape = CircleShape)
        )
        Text(
            text = labelText,
            color = token.content,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
