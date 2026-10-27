package com.aditya.smartmedbox.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxLoadingIndicator
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar
import com.aditya.smartmedbox.core.designsystem.theme.LocalSpacing

@Composable
fun AuthScreen(uiState: AuthUiState, onEvent: (AuthUiEvent) -> Unit, onNavigateToDashboard: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current

    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(title = "Sign In & Device Link")
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (uiState) {
            is AuthUiState.Loading -> {
                SmartMedBoxLoadingIndicator(modifier = Modifier.padding(innerPadding))
            }
            else -> {
                Column(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(spacing.medium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Smart Medicine Box",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Connect your device and manage prescriptions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = spacing.small, bottom = spacing.large)
                    )

                    Button(
                        onClick = {
                            onEvent(AuthUiEvent.SignIn("demo@smartmedbox.com"))
                            onNavigateToDashboard()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Continue to Dashboard",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
