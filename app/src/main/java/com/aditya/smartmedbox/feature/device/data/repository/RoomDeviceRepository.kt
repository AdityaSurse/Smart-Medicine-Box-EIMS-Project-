package com.aditya.smartmedbox.feature.device.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomDeviceRepository @Inject constructor(
    private val deviceStatusDao: DeviceStatusDao
) : DeviceRepository {

    override fun observeDiscoveredDevice(): Flow<SmartBoxDevice?> {
        return deviceStatusDao.getDeviceStatusFlow("DG_BOX_4081").map { entity ->
            if (entity != null) {
                SmartBoxDevice(
                    deviceId = entity.deviceId,
                    serialNumber = "#DG-4081",
                    modelName = "DoseGuard Box #DG-4081",
                    connectionType = "Bluetooth LE • Smart Dispenser 7-Bay",
                    signalStrength = if (entity.isOnline) "Strong Signal" else "Offline",
                    firmwareVersion = "v3.2",
                    pairingCode = "8820",
                    isNearby = entity.isOnline,
                    isPaired = true
                )
            } else {
                null
            }
        }
    }

    override suspend fun startScan(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun pairDevice(deviceId: String): Result<Unit> {
        return try {
            deviceStatusDao.updateOnlineStatus(deviceId, isOnline = true, timestamp = System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disconnectDevice(deviceId: String): Result<Unit> {
        return try {
            deviceStatusDao.updateOnlineStatus(deviceId, isOnline = false, timestamp = System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
