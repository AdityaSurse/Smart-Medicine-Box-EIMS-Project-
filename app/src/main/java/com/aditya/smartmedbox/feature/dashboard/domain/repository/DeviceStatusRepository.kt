package com.aditya.smartmedbox.feature.dashboard.domain.repository

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceStatus
import kotlinx.coroutines.flow.Flow

interface DeviceStatusRepository {
    fun getDeviceStatus(): Flow<DeviceStatus>
    suspend fun updateDeviceStatus(status: DeviceStatus): Result<Unit>
}
