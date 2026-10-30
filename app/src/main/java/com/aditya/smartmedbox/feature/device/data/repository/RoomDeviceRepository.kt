package com.aditya.smartmedbox.feature.device.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

@Singleton
class RoomDeviceRepository @Inject constructor(
    private val deviceStatusDao: DeviceStatusDao,
    private val authRepository: AuthRepository,
    private val firestoreDeviceDataSource: FirestoreDeviceDataSource
) : DeviceRepository {

    override fun observeDiscoveredDevice(): Flow<SmartBoxDevice?> {
        return deviceStatusDao.getLatestDeviceStatus().map { entity ->
            if (entity != null) {
                SmartBoxDevice(
                    deviceId = entity.deviceId,
                    serialNumber = "#${entity.deviceId}",
                    modelName = "DoseGuard Box #${entity.deviceId}",
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

    override suspend fun pairDevice(deviceId: String, pairingToken: String): Result<Unit> {
        return try {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                firestoreDeviceDataSource.claimDevice(
                    uid = user.uid,
                    deviceId = deviceId,
                    pairingToken = pairingToken
                )
            }
            authRepository.updateLinkedDeviceId(deviceId)
            deviceStatusDao.upsertDeviceStatus(
                DeviceStatusEntity(
                    deviceId = deviceId,
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unpairDevice(deviceId: String): Result<Unit> {
        return try {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                firestoreDeviceDataSource.unpairDevice(uid = user.uid, deviceId = deviceId)
            }
            authRepository.updateLinkedDeviceId(null)
            deviceStatusDao.deleteDevice(deviceId)
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
