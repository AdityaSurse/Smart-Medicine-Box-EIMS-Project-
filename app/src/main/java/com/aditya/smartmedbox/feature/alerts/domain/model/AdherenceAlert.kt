package com.aditya.smartmedbox.feature.alerts.domain.model

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}

data class AdherenceAlert(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val severity: AlertSeverity
)
