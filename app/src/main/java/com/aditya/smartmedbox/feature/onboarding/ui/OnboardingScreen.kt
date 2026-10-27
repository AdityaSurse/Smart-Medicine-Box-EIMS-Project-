package com.aditya.smartmedbox.feature.onboarding.ui

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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.BluetoothSearching
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.aditya.smartmedbox.R
import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxPrimaryButton
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxStatusChip
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onEvent: (OnboardingUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.marginMobile, vertical = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.large + spacing.small)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = "DoseGuard Logo",
                            tint = MaterialTheme.colorScheme.surfaceContainerLowest,
                            modifier = Modifier.size(spacing.large)
                        )
                    }
                    Text(
                        text = "DoseGuard Setup",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { onEvent(OnboardingUiEvent.Troubleshoot) },
                    modifier = Modifier.semantics { contentDescription = "Setup Help" }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = spacing.marginMobile)
        ) {
            when (uiState) {
                is OnboardingUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Scanning for nearby DoseGuard Smart Medicine Box...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OnboardingUiState.Empty -> {
                    SmartMedBoxEmptyState(
                        title = "No Smart Box Detected",
                        message = "Ensure the Smart Box is powered on, BLE is enabled on your phone, and place it within 3 feet.",
                        actionButtonText = "Scan Again",
                        onActionClick = { onEvent(OnboardingUiEvent.DiscoverDevices) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OnboardingUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Pairing Error",
                        message = uiState.message,
                        onRetry = { onEvent(OnboardingUiEvent.Retry) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OnboardingUiState.Offline -> {
                    SmartMedBoxErrorState(
                        title = "Bluetooth Offline",
                        message = "Please turn on Bluetooth to connect to your Smart Box.",
                        onRetry = { onEvent(OnboardingUiEvent.DiscoverDevices) },
                        retryButtonText = "Enable & Retry",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is OnboardingUiState.Content -> {
                    OnboardingContent(
                        state = uiState,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    state: OnboardingUiState.Content,
    onEvent: (OnboardingUiEvent) -> Unit
) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        // 1. 3-STEP PROGRESS INDICATOR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepIndicator(stepNumber = 1, label = "Welcome", isCompleted = true, isActive = false)
                StepIndicator(stepNumber = 2, label = "Connect Box", isCompleted = false, isActive = true)
                StepIndicator(stepNumber = 3, label = "Medicine", isCompleted = false, isActive = false)
            }
        }

        // 2. STEP EYEBROW & EDITORIAL HEADLINE
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.small)
                        .background(MaterialTheme.colorScheme.secondary, CircleShape)
                )
                Text(
                    text = "STEP 02 OF 03 • HARDWARE PAIRING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Connecting to your Smart Box",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Place your phone within 3 feet of the smart box. Ensure the power cable is connected and the top lid ring is pulsing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 3. CONCENTRIC RADAR PULSE / SCANNING VISUAL
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.large),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .size(spacing.huge + spacing.extraLarge)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                        .border(spacing.hairlineBorder, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.huge)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.BluetoothSearching,
                            contentDescription = "Bluetooth Searching Node",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(spacing.large)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Box(
                        modifier = Modifier
                            .size(spacing.small)
                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    )
                    Text(
                        text = "NEARBY SIGNAL LOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // 4. FOUND BOX CARD (ACTIVE DISCOVERED STATE)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                    verticalAlignment = Alignment.Top
                ) {
                    // Smart Box Vector Illustration
                    Box(
                        modifier = Modifier
                            .size(spacing.huge + spacing.small)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, ControlShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_smart_box),
                            contentDescription = "DoseGuard Smart Box",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(spacing.huge)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = state.device.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            SmartMedBoxStatusChip(status = DoseStatus.ON_TIME, customLabel = "ACTIVE")
                        }

                        Text(
                            text = "Bluetooth LE • Smart Dispenser 7-Bay",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.small),
                            modifier = Modifier.padding(top = spacing.extraSmall)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SignalCellularAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(spacing.medium)
                            )
                            Text(
                                text = state.device.signalStrength,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "•  Firmware ${state.device.firmwareVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Verified Pairing Micro-Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = spacing.medium, vertical = spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = "Verified Pairing Code",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(spacing.medium)
                        )
                        Text(
                            text = "Pairing code verified",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = state.device.pairingCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceContainer, PillShape)
                            .padding(horizontal = spacing.small, vertical = spacing.extraSmall)
                    )
                }
            }
        }

        // 5. ACTION BUTTONS
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.small),
            modifier = Modifier.padding(top = spacing.small)
        ) {
            SmartMedBoxPrimaryButton(
                text = if (state.isPairingInProgress) "Configuring Bays..." else "Continue to Add Medicine",
                onClick = { onEvent(OnboardingUiEvent.ConfirmPairing(state.device.pairingCode)) },
                trailingIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                enabled = !state.isPairingInProgress
            )

            TextButton(
                onClick = { onEvent(OnboardingUiEvent.Troubleshoot) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spacing.minTouchTarget)
            ) {
                Text(
                    text = "Troubleshoot connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.medium))
    }
}

@Composable
private fun StepIndicator(
    stepNumber: Int,
    label: String,
    isCompleted: Boolean,
    isActive: Boolean
) {
    val spacing = LocalSpacing.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
    ) {
        Box(
            modifier = Modifier
                .size(spacing.large + spacing.extraSmall)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> MaterialTheme.colorScheme.secondaryContainer
                        isActive -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(spacing.medium)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
