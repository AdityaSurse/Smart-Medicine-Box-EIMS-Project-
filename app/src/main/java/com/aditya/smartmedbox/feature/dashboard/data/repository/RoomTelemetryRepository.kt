package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
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
    private val medicineDao: MedicineDao
) : TelemetryRepository {

    override fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry> {
        return combine(
            deviceStatusDao.getDeviceStatusFlow("DG_BOX_4081"),
            medicineDao.getAllMedicines()
        ) { statusEntity, medicines ->
            val totalTablets = medicines.sumOf { it.currentStock }
            if (statusEntity != null) {
                DeviceTelemetry(
                    deviceId = statusEntity.deviceId,
                    isOnline = statusEntity.isOnline,
                    isBoxOpen = statusEntity.isBoxOpen,
                    temperatureCelsius = statusEntity.temperatureCelsius,
                    humidityPercent = statusEntity.humidityPercent,
                    weightGrams = statusEntity.weightGrams,
                    timestamp = statusEntity.lastSeenTimestamp,
                    tabletsRemaining = totalTablets,
                    batteryPercent = statusEntity.batteryPercent
                )
            } else {
                DeviceTelemetry(tabletsRemaining = totalTablets)
            }
        }
    }

    override suspend fun refreshTelemetry(deviceId: String): Result<DeviceTelemetry> {
        return try {
            val current = deviceStatusDao.getDeviceStatusFlow("DG_BOX_4081").firstOrNull()
            val updated = (
                current ?: com.aditya.smartmedbox.core.database.entity.DeviceStatusEntity(
                    deviceId = "DG_BOX_4081",
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
            val totalTablets = medicines.sumOf { it.currentStock }

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
            val current = deviceStatusDao.getDeviceStatusFlow("DG_BOX_4081").firstOrNull()
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
}
