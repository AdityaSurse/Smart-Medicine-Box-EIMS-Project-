package com.aditya.smartmedbox.feature.auth.ui

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingState
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxPrimaryButton
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.CardShape
import com.aditya.smartmedbox.core.designsystem.theme.ControlShape
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing
import com.aditya.smartmedbox.core.designsystem.theme.PillShape

@Composable
fun AuthScreen(uiState: AuthUiState, onEvent: (AuthUiEvent) -> Unit, onNavigateToDashboard: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(
                title = "DoseGuard",
                subtitle = "Access & Authentication"
            )
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
                is AuthUiState.Loading -> {
                    SmartMedBoxLoadingState(
                        message = "Verifying apothecary credentials...",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AuthUiState.Offline -> {
                    Text(
                        text = "Device is currently offline. Please reconnect.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is AuthUiState.Content -> {
                    AuthFormContent(
                        state = uiState,
                        onEvent = onEvent,
                        onNavigateToDashboard = onNavigateToDashboard
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFormContent(state: AuthUiState.Content, onEvent: (AuthUiEvent) -> Unit, onNavigateToDashboard: () -> Unit) {
    val spacing = LocalSpacing.current
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(spacing.small))

        // 1. BRAND ICON & APOTHECARY SUBTITLE
        Box(
            modifier = Modifier
                .size(spacing.huge + spacing.small)
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
            text = if (state.isSignUpMode) "Register Smart Box" else "Welcome Back",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = if (state.isSignUpMode) {
                "Create a caregiver or patient profile to pair and synchronize with the medicine box."
            } else {
                "Sign in to synchronize your medication regimen with Box #DG-4081."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = spacing.small)
        )

        // 2. TOGGLE TABS (SIGN IN / REGISTER)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PillShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(spacing.extraSmall)
        ) {
            val signInSelected = !state.isSignUpMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(
                        if (signInSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                    .padding(vertical = spacing.small),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { if (state.isSignUpMode) onEvent(AuthUiEvent.ToggleAuthMode) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (signInSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            val registerSelected = state.isSignUpMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(
                        if (registerSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                    .padding(vertical = spacing.small),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { if (!state.isSignUpMode) onEvent(AuthUiEvent.ToggleAuthMode) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (registerSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }

        // 3. INPUT FORM CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .border(spacing.hairlineBorder, MaterialTheme.colorScheme.outlineVariant, CardShape)
                .padding(spacing.medium)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
                // Email Field
                Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                    Text(
                        text = "EMAIL ADDRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { onEvent(AuthUiEvent.EmailChanged(it)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = { Text("e.g. name@apothecary.care") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = ControlShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Email address input" }
                    )
                }

                // Password Field
                Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                    Text(
                        text = "SECURE PASSCODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { onEvent(AuthUiEvent.PasswordChanged(it)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            onEvent(AuthUiEvent.Submit)
                            onNavigateToDashboard()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = ControlShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Passcode input" }
                    )
                }

                // Error alert if present
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = spacing.extraSmall)
                    )
                }
            }
        }

        // 4. ACTION BUTTONS
        SmartMedBoxPrimaryButton(
            text = if (state.isSignUpMode) "Create Account & Pair" else "Sign In to Dispensary",
            onClick = {
                onEvent(AuthUiEvent.Submit)
                onNavigateToDashboard()
            },
            enabled = !state.isLoading
        )

        androidx.compose.material3.OutlinedButton(
            onClick = {
                onEvent(AuthUiEvent.SignInWithGoogle())
                onNavigateToDashboard()
            },
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = PillShape
        ) {
            Text(
                text = "Continue with Google",
                style = MaterialTheme.typography.labelLarge
            )
        }

        // 5. CAREGIVER SECURITY FOOTER NOTE
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            modifier = Modifier.padding(top = spacing.small)
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(spacing.medium)
            )
            Text(
                text = "HIPAA & DISPENSER TELEMETRY ENCRYPTED",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(spacing.medium))
    }
}
