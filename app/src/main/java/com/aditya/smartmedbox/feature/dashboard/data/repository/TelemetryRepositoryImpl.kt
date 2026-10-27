package com.aditya.smartmedbox.feature.dashboard.data.repository

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Singleton
class TelemetryRepositoryImpl
@Inject
constructor() : TelemetryRepository {
    override fun observeTelemetry(deviceId: String): Flow<DeviceTelemetry> {
        return flowOf(
            DeviceTelemetry(
                deviceId = deviceId,
                weightGrams = 142.5f,
                isBoxOpen = false,
                temperatureCelsius = 23.8f,
                humidityPercent = 46.2f,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
