package com.aditya.smartmedbox.feature.alerts.domain.repository

import com.aditya.smartmedbox.feature.alerts.domain.model.AdherenceAlert
import kotlinx.coroutines.flow.Flow

interface AlertsRepository {
    fun getActiveAlerts(): Flow<List<AdherenceAlert>>
    suspend fun markDoseTaken(alertId: String): Result<Unit>
    suspend fun snoozeAlert(alertId: String, minutes: Int): Result<Unit>
    suspend fun skipDose(alertId: String, reason: String?): Result<Unit>
}
