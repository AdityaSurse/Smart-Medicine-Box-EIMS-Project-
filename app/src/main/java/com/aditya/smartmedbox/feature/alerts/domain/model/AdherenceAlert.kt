package com.aditya.smartmedbox.feature.alerts.domain.model

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class TrayCompartmentStatus(
    val trayId: String,
    val timeOfDay: String,
    val statusText: String,
    val isMissedOrUnopened: Boolean = false,
    val isTaken: Boolean = false
)

data class AdherenceAlert(
    val id: String,
    val title: String,
    val medicineName: String,
    val dosage: String,
    val instructions: String,
    val compartmentTag: String,
    val scheduledTime: String,
    val delayMinutes: Int,
    val boxSerial: String = "#DG-4081",
    val lastSyncTime: String = "2 min ago",
    val severity: AlertSeverity = AlertSeverity.CRITICAL,
    val trays: List<TrayCompartmentStatus> = emptyList(),
    val isChimeActive: Boolean = true
)
