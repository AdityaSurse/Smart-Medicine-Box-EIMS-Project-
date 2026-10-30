package com.aditya.smartmedbox.feature.device.data.repository

import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeDeviceRepository @Inject constructor() : DeviceRepository {

    private val deviceState = MutableStateFlow<SmartBoxDevice?>(
        SmartBoxDevice(
            deviceId = "ble_dg_4081",
            serialNumber = "#DG-4081",
            modelName = "DoseGuard Box #DG-4081",
            connectionType = "Bluetooth LE • Smart Dispenser 7-Bay",
            signalStrength = "Strong Signal",
            firmwareVersion = "v3.2",
            pairingCode = "8820",
            isNearby = true,
            isPaired = true
        )
    )

    override fun observeDiscoveredDevice(): Flow<SmartBoxDevice?> = deviceState.asStateFlow()

    override suspend fun startScan(): Result<Unit> {
        delay(600)
        return Result.success(Unit)
    }

    override suspend fun pairDevice(deviceId: String, pairingToken: String): Result<Unit> {
        delay(800)
        val current = deviceState.value
        deviceState.value = (current ?: SmartBoxDevice(deviceId = deviceId)).copy(
            deviceId = deviceId,
            pairingCode = pairingToken.ifBlank { "8820" },
            isPaired = true,
            isNearby = true
        )
        return Result.success(Unit)
    }

    override suspend fun unpairDevice(deviceId: String): Result<Unit> {
        delay(400)
        deviceState.value = null
        return Result.success(Unit)
    }

    override suspend fun disconnectDevice(deviceId: String): Result<Unit> {
        deviceState.value = null
        return Result.success(Unit)
    }
}
