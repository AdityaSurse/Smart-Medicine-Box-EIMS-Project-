package com.aditya.smartmedbox.feature.settings.domain.model

data class CaregiverContact(
    val id: String,
    val name: String,
    val relationship: String = "Primary Caregiver",
    val phoneNumber: String,
    val isAlertsEnabled: Boolean = true,
    val initials: String = "SJ"
)

data class HardwareBayStatus(
    val bayId: String,
    val label: String,
    val isTaken: Boolean,
    val iconName: String
)

data class DeviceSettings(
    val deviceName: String = "DoseGuard Bay 4X",
    val hardwareId: String = "HW #04",
    val serialNumber: String = "#DG-4081-B",
    val firmwareVersion: String = "v2.4.1",
    val isOnline: Boolean = true,
    val lastSyncTime: String = "2 min ago",
    val batteryPercent: Int = 84,
    val wifiSsid: String = "Apothecary_Guest_5G",
    val soundChimeName: String = "Gentle Chime (65dB)",
    val snoozeIntervalMinutes: Int = 10,
    val refillThresholdTablets: Int = 5,
    val isDarkMode: Boolean = false,
    val unitSystem: String = "Metric (°C, g)",
    val gracePeriodMinutes: Int = 30,
    val reminderRepeatCount: Int = 3,
    val quietHoursEnabled: Boolean = false,
    val quietHoursText: String = "10:00 PM – 07:00 AM",
    val bays: List<HardwareBayStatus> = listOf(
        HardwareBayStatus("BAY 01", "MORN", true, "check_circle"),
        HardwareBayStatus("BAY 02", "NOON", true, "check_circle"),
        HardwareBayStatus("BAY 03", "EVE", false, "schedule"),
        HardwareBayStatus("BAY 04", "BED", false, "bedtime")
    ),
    val caregivers: List<CaregiverContact> = listOf(
        CaregiverContact(
            id = "cg_01",
            name = "Sarah Jenkins",
            relationship = "Primary Caregiver",
            phoneNumber = "+1 (555) 234-5678",
            isAlertsEnabled = true,
            initials = "SJ"
        )
    )
)
