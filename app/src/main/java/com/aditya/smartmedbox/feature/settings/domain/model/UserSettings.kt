package com.aditya.smartmedbox.feature.settings.domain.model

data class UserSettings(
    val userName: String = "Eleanor Vance",
    val isDarkMode: Boolean = false,
    val snoozeIntervalMinutes: Int = 10,
    val lowStockThresholdDefault: Int = 5,
    val isChimeEnabled: Boolean = true,
    val primaryCaregiverName: String = "Sarah Jenkins",
    val primaryCaregiverPhone: String = "+1 (555) 234-5678"
)
