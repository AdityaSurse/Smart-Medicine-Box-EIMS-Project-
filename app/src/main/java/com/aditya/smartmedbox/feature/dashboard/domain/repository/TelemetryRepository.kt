package com.aditya.smartmedbox.feature.dashboard.domain.repository

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry>
    suspend fun refreshTelemetry(deviceId: String): Result<DeviceTelemetry>
    suspend fun dispenseDose(tray: String): Result<Unit>
    suspend fun silenceBuzzer(deviceId: String): Result<Unit>
}
