package com.aditya.smartmedbox.feature.device.domain.repository

import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun observeDiscoveredDevice(): Flow<SmartBoxDevice?>
    suspend fun startScan(): Result<Unit>
    suspend fun pairDevice(deviceId: String, pairingToken: String = ""): Result<Unit>
    suspend fun unpairDevice(deviceId: String): Result<Unit>
    suspend fun disconnectDevice(deviceId: String): Result<Unit>
}
