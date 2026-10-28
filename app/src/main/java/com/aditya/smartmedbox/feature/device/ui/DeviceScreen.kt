package com.aditya.smartmedbox.feature.device.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxEmptyState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxErrorState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape
import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice

@Composable
fun DeviceScreen(
    uiState: DeviceUiState,
    onEvent: (DeviceUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAddMedicine: () -> Unit = {}
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
                            .size(36.dp)
                            .clip(ControlShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "DoseGuard Setup",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(
                    onClick = { onEvent(DeviceUiEvent.Troubleshoot) },
                    modifier = Modifier.semantics { contentDescription = "Support and instructions" }
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
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
        ) {
            when (uiState) {
                is DeviceUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Scanning for nearby DoseGuard boxes via Bluetooth LE...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DeviceUiState.Error -> {
                    SmartMedBoxErrorState(
                        title = "Hardware Discovery Error",
                        message = uiState.message,
                        onRetry = { onEvent(DeviceUiEvent.StartScan) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }

                is DeviceUiState.Offline -> {
                    SmartMedBoxErrorState(
                        title = "Bluetooth Adapter Offline",
                        message = uiState.message,
                        onRetry = { onEvent(DeviceUiEvent.StartScan) },
                        retryButtonText = "Retry Scanning",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(spacing.marginMobile)
                    )
                }

                is DeviceUiState.Empty -> {
                    SmartMedBoxEmptyState(
                        title = "No Smart Boxes Discovered",
                        message = uiState.message,
                        actionButtonText = "Scan Again",
                        onActionClick = { onEvent(DeviceUiEvent.StartScan) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DeviceUiState.Success -> {
                    DevicePairingContent(
                        device = uiState.device,
                        isPairing = uiState.isPairingInProgress,
                        feedbackMessage = uiState.pairingSuccessMessage,
                        onPairClick = {
                            onEvent(DeviceUiEvent.PairDevice(uiState.device.deviceId))
                            onNavigateToAddMedicine()
                        },
                        onTroubleshootClick = { onEvent(DeviceUiEvent.Troubleshoot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DevicePairingContent(
    device: SmartBoxDevice,
    isPairing: Boolean,
    feedbackMessage: String?,
    onPairClick: () -> Unit,
    onTroubleshootClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.marginMobile, vertical = spacing.small),
        verticalArrangement = Arrangement.spacedBy(spacing.large)
    ) {
        // 3-Step Progress Indicator
        ThreeStepProgressIndicator(currentStep = 2)

        // Step Eyebrow & Editorial Headline
        Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    text = "STEP 02 OF 03 • HARDWARE PAIRING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Connecting to your Smart Box",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Place your phone within 3 feet of the smart box. Ensure the power cable is connected and the top lid ring is pulsing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Radar pulse visual
        ConcentricRadarScanningVisual()

        // Found Box Card
        DiscoveredBoxCard(device = device)

        // Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Button(
                onClick = onPairClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = CardShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isPairing
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    if (isPairing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = feedbackMessage ?: "Configuring Bays...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Continue to Add Medicine",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            TextButton(
                onClick = onTroubleshootClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Troubleshoot connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ThreeStepProgressIndicator(currentStep: Int) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Step 1: Welcome (Complete)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Step 1 Welcome completed",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "WELCOME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider line 1
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = spacing.extraSmall)
            )

            // Step 2: Connect Box (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "CONNECT BOX",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider line 2
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
                    .padding(horizontal = spacing.extraSmall)
            )

            // Step 3: Medicine (Upcoming)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "MEDICINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ConcentricRadarScanningVisual() {
    val spacing = LocalSpacing.current

    val infiniteTransition = rememberInfiniteTransition(label = "RadarTransition")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadarPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.large),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium)
        ) {
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                // Concentric circles
                val ringColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    drawCircle(color = ringColor, radius = size.minDimension / 2 * pulseSize, style = Stroke(width = 1.5f))
                    drawCircle(color = ringColor, radius = size.minDimension / 2.6f, style = Stroke(width = 1.2f))
                    drawCircle(color = ringColor, radius = size.minDimension / 3.6f, style = Stroke(width = 1f))
                }

                // Center Bluetooth node
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BluetoothSearching,
                        contentDescription = "Bluetooth searching signal",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    text = "NEARBY SIGNAL LOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DiscoveredBoxCard(device: SmartBoxDevice) {
    val spacing = LocalSpacing.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape)
            .padding(spacing.medium)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.medium),
                verticalAlignment = Alignment.Top
            ) {
                // Smart Box 7-bay vector graphic canvas
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, ControlShape),
                    contentAlignment = Alignment.Center
                ) {
                    val lineColor = MaterialTheme.colorScheme.onSurface
                    val lidDotColor = MaterialTheme.colorScheme.secondary
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawRoundRect(
                            color = lineColor,
                            topLeft = Offset(4f, 8f),
                            size = Size(size.width - 8f, size.height - 16f),
                            style = Stroke(width = 2f)
                        )
                        drawLine(
                            color = lineColor,
                            start = Offset(size.width / 3, 8f),
                            end = Offset(size.width / 3, size.height - 8f),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = lineColor,
                            start = Offset(size.width * 2 / 3, 8f),
                            end = Offset(size.width * 2 / 3, size.height - 8f),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = lineColor,
                            start = Offset(4f, size.height / 2),
                            end = Offset(size.width - 4f, size.height / 2),
                            strokeWidth = 1.5f
                        )
                        drawCircle(
                            color = lidDotColor,
                            radius = 3.5f,
                            center = Offset(size.width / 2, 14f)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = device.modelName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = spacing.small, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = device.connectionType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(spacing.extraSmall))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(spacing.small)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = device.signalStrength,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "Firmware ${device.firmwareVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Verification Micro-banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ControlShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, ControlShape)
                    .padding(horizontal = spacing.medium, vertical = spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pairing code verified",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(ControlShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = spacing.small, vertical = 2.dp)
                ) {
                    Text(
                        text = device.pairingCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

