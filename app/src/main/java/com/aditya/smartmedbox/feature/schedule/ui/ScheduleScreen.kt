package com.aditya.smartmedbox.feature.schedule.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aditya.smartmedbox.core.designsystem.component.SmartMedBoxTopAppBar

@Composable
fun ScheduleScreen(modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            SmartMedBoxTopAppBar(title = "Prescription Schedules")
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Prescription Schedule Management",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
