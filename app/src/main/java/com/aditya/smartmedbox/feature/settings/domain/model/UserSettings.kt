package com.aditya.smartmedbox.feature.settings.domain.model

data class UserSettings(
    val userName: String = "Eleanor Vance",
    val isDarkMode: Boolean = false,
    val snoozeIntervalMinutes: Int = 10,
    val maxReminderRepeats: Int = 3,
    val lowStockThresholdDefault: Int = 5,
    val isChimeEnabled: Boolean = true,
    val primaryCaregiverName: String = "Sarah Jenkins",
    val primaryCaregiverPhone: String = "+1 (555) 234-5678",
    val unitSystem: String = "Metric (°C, g)",
    val gracePeriodMinutes: Int = 30,
    val quietHoursEnabled: Boolean = false,
    val quietHoursText: String = "10:00 PM – 07:00 AM"
)
