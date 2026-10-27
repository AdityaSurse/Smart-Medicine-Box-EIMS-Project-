package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class TelemetryRepositoryImpl @Inject constructor() : TelemetryRepository {
    private val _telemetry = MutableStateFlow(DeviceTelemetry())

    override fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry> = _telemetry.asStateFlow()

    override suspend fun refreshTelemetry(deviceId: String): Result<DeviceTelemetry> {
        delay(600) // Simulate network/BLE latency
        val updated = _telemetry.value.copy(
            timestamp = System.currentTimeMillis(),
            temperatureCelsius = 28.4f,
            humidityPercent = 55.0f,
            isOnline = true
        )
        _telemetry.value = updated
        return Result.success(updated)
    }

    override suspend fun dispenseDose(tray: String): Result<Unit> {
        delay(800)
        val current = _telemetry.value
        _telemetry.value = current.copy(
            isBoxOpen = true,
            tabletsRemaining = (current.tabletsRemaining - 1).coerceAtLeast(0)
        )
        return Result.success(Unit)
    }
}
