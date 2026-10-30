package com.aditya.smartmedbox.feature.alerts.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPermissionRationaleScreen(onPermissionGranted: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onDismiss()
        }
    }

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "Alerts & Reminders",
                subtitle = "Permission Safeguard",
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.semantics { contentDescription = "Go back" }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Navigate back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.marginMobile),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Spacer(modifier = Modifier.height(spacing.small))

            // Apothecary Medical Hero Header
            Box(
                modifier = Modifier
                    .size(spacing.huge * 1.5f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(spacing.hairlineBorder, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsActive,
                    contentDescription = "Notification Alerts",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(spacing.huge)
                )
            }

            Text(
                text = "Prescription Protection",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your Smart Medicine Box relies on time-sensitive notifications to protect medication adherence and storage integrity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(spacing.extraSmall))

            // Rationale Benefit Cards
            RationaleBenefitItem(
                icon = Icons.Outlined.Schedule,
                title = "Exact-Time Medication Reminders",
                description = "Urgent alerts ring at scheduled times with physical box tray indicators so you never miss a dose."
            )

            RationaleBenefitItem(
                icon = Icons.Outlined.Security,
                title = "Grace Period Escalations",
                description = "If the compartment lid is not opened within the grace window, alerts escalate to prevent omitted prescriptions."
            )

            RationaleBenefitItem(
                icon = Icons.Outlined.DeviceThermostat,
                title = "Chamber Climate & Hardware Alerts",
                description = "Immediate warnings if box temperature or moisture exceeds safe thresholds, preserving pill potency."
            )

            RationaleBenefitItem(
                icon = Icons.Outlined.Inventory2,
                title = "Low Stock Refill Prompts",
                description = "Convenient gentle notifications when pill counts drop below your buffer threshold."
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.medium),
                verticalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onPermissionGranted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.huge),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(spacing.huge),
                    shape = PillShape
                ) {
                    Text(
                        text = "Continue Without Alerts",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RationaleBenefitItem(icon: ImageVector, title: String, description: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            verticalAlignment = Alignment.Top
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(spacing.large)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(spacing.extraSmall))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
