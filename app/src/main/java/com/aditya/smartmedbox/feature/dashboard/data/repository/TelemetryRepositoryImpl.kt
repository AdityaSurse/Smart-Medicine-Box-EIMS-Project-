package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.firebase.datasource.FirestoreDeviceDataSource
import com.aditya.smartmedbox.core.util.DispatcherProvider
import com.aditya.smartmedbox.feature.auth.domain.repository.AuthRepository
import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@Singleton
class TelemetryRepositoryImpl @Inject constructor(
    private val deviceStatusDao: DeviceStatusDao,
    private val medicineDao: MedicineDao,
    private val authRepository: AuthRepository,
    private val firestoreDeviceDataSource: FirestoreDeviceDataSource,
    private val dispatchers: DispatcherProvider
) : TelemetryRepository {

    override fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry> {
        return combine(
            deviceStatusDao.getLatestDeviceStatus(),
            medicineDao.getAllMedicines(),
            authRepository.getAuthState()
        ) { statusEntity, medicinesList, authUser ->
            val now = System.currentTimeMillis()
            val lastSeen = statusEntity?.lastSeenTimestamp ?: now
            val isStale = (now - lastSeen) > (5 * 60 * 1000L)

            val diffMinutes = ((now - lastSeen) / 60_000L).coerceAtLeast(0)
            val lastSeenFormatted = when {
                diffMinutes < 1 -> "Just now"
                diffMinutes < 60 -> "${diffMinutes}m ago"
                else -> "${diffMinutes / 60}h ago"
            }

            val totalActiveTablets = medicinesList.filter { it.isActive }.sumOf { it.currentStock }

            DeviceTelemetry(
                deviceId = statusEntity?.deviceId ?: deviceId,
                patientName = authUser?.displayName ?: "Eleanor",
                weightGrams = statusEntity?.weightGrams ?: 14.8f,
                isBoxOpen = statusEntity?.isBoxOpen ?: false,
                temperatureCelsius = statusEntity?.temperatureCelsius ?: 21.4f,
                humidityPercent = statusEntity?.humidityPercent ?: 46.0f,
                tabletsRemaining = totalActiveTablets,
                cadenceStatus = "On time",
                isOnline = statusEntity?.isOnline ?: true,
                batteryPercent = statusEntity?.batteryPercent ?: 84,
                timestamp = lastSeen,
                isStale = isStale,
                lastSeenFormatted = lastSeenFormatted
            )
        }
    }

    override suspend fun refreshTelemetry(deviceId: String): Result<DeviceTelemetry> = withContext(dispatchers.io) {
        val user = authRepository.getAuthState().firstOrNull()
        if (user != null) {
            val remoteTelemetry = firestoreDeviceDataSource.observeLatestTelemetry(user.uid, deviceId).firstOrNull()
            if (remoteTelemetry != null) {
                deviceStatusDao.updateOnlineStatus(deviceId, isOnline = true, timestamp = System.currentTimeMillis())
            }
        }
        val current = observeTelemetry(deviceId).firstOrNull() ?: DeviceTelemetry(deviceId = deviceId)
        Result.success(current)
    }

    override suspend fun dispenseDose(tray: String): Result<Unit> = withContext(dispatchers.io) {
        val user = authRepository.getAuthState().firstOrNull()
        if (user != null) {
            firestoreDeviceDataSource.sendDeviceCommand(
                uid = user.uid,
                deviceId = user.linkedDeviceId ?: "DG-4081",
                commandType = "DISPENSE_DOSE",
                payload = mapOf("tray" to tray)
            )
        }
        Result.success(Unit)
    }

    override suspend fun silenceBuzzer(deviceId: String): Result<Unit> = withContext(dispatchers.io) {
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
    }
}
