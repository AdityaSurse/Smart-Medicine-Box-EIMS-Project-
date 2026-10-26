package com.aditya.smartmedbox.feature.dashboard.domain.repository

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import kotlinx.coroutines.flow.Flow

interface TelemetryRepository {
    fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry>
}
