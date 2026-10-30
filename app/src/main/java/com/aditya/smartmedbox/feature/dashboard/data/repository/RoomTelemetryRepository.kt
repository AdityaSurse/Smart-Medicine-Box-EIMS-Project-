package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class RoomTelemetryRepository @Inject constructor(
    private val deviceStatusDao: DeviceStatusDao,
    private val medicineDao: MedicineDao,
    private val authRepository: AuthRepository,
    private val firestoreDeviceDataSource: FirestoreDeviceDataSource
) : TelemetryRepository {

    override fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry> {
        return combine(
            deviceStatusDao.getLatestDeviceStatus(),
            medicineDao.getAllMedicines(),
            authRepository.getAuthState()
        ) { statusEntity, medicines, authUser ->
            val totalTablets = medicines.filter { it.isActive }.sumOf { it.currentStock }
            val now = System.currentTimeMillis()
            val lastSeen = statusEntity?.lastSeenTimestamp ?: now
            val isStale = (now - lastSeen) > (5 * 60 * 1000L)

            val diffMinutes = ((now - lastSeen) / 60_000L).coerceAtLeast(0)
            val lastSeenFormatted = when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                else -> "${diffMinutes / 60}h ago"
            }

            if (statusEntity != null) {
                DeviceTelemetry(
                    deviceId = statusEntity.deviceId,
                    patientName = authUser?.displayName ?: "Eleanor",
                    isOnline = statusEntity.isOnline,
                    isBoxOpen = statusEntity.isBoxOpen,
                    temperatureCelsius = statusEntity.temperatureCelsius,
                    humidityPercent = statusEntity.humidityPercent,
                    weightGrams = statusEntity.weightGrams,
                    timestamp = statusEntity.lastSeenTimestamp,
                    tabletsRemaining = totalTablets,
                    batteryPercent = statusEntity.batteryPercent,
                    isStale = isStale,
                    lastSeenFormatted = lastSeenFormatted
                )
            } else {
                DeviceTelemetry(
                    deviceId = deviceId,
                    patientName = authUser?.displayName ?: "Eleanor",
                    tabletsRemaining = totalTablets,
                    isStale = isStale,
                    lastSeenFormatted = lastSeenFormatted
                )
            }
        }
    }

    override suspend fun refreshTelemetry(deviceId: String): Result<DeviceTelemetry> {
        return try {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                val remoteTelemetry = firestoreDeviceDataSource.observeLatestTelemetry(user.uid, deviceId).firstOrNull()
                if (remoteTelemetry != null) {
                    deviceStatusDao.updateOnlineStatus(deviceId, isOnline = true, timestamp = System.currentTimeMillis())
                }
            }

            val current = deviceStatusDao.getLatestDeviceStatus().firstOrNull()
            val updated = (
                current ?: DeviceStatusEntity(
                    deviceId = deviceId,
                    temperatureCelsius = 21.4f,
                    humidityPercent = 46.0f,
                    weightGrams = 14.8f,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    isBoxOpen = false,
                    batteryPercent = 84,
                    isOnline = true
                )
                ).copy(
                lastSeenTimestamp = System.currentTimeMillis(),
                isOnline = true
            )
            deviceStatusDao.upsertDeviceStatus(updated)

            val medicines = medicineDao.getAllMedicines().firstOrNull() ?: emptyList()
            val totalTablets = medicines.filter { it.isActive }.sumOf { it.currentStock }

            Result.success(
                DeviceTelemetry(
                    deviceId = updated.deviceId,
                    isOnline = updated.isOnline,
                    isBoxOpen = updated.isBoxOpen,
                    temperatureCelsius = updated.temperatureCelsius,
                    humidityPercent = updated.humidityPercent,
                    weightGrams = updated.weightGrams,
                    timestamp = updated.lastSeenTimestamp,
                    tabletsRemaining = totalTablets,
                    batteryPercent = updated.batteryPercent
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun dispenseDose(tray: String): Result<Unit> {
        return try {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                firestoreDeviceDataSource.sendDeviceCommand(
                    uid = user.uid,
                    deviceId = user.linkedDeviceId ?: "DG-4081",
                    commandType = "DISPENSE_DOSE",
                    payload = mapOf("tray" to tray)
                )
            }
            val current = deviceStatusDao.getLatestDeviceStatus().firstOrNull()
            if (current != null) {
                deviceStatusDao.upsertDeviceStatus(
                    current.copy(
                        isBoxOpen = true,
                        lastSeenTimestamp = System.currentTimeMillis()
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun silenceBuzzer(deviceId: String): Result<Unit> {
        return try {
            val user = authRepository.getAuthState().firstOrNull()
            if (user != null) {
                firestoreDeviceDataSource.sendDeviceCommand(
                    uid = user.uid,
                    deviceId = deviceId,
                    commandType = "SILENCE_BUZZER",
                    payload = emptyMap()
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
