package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import com.aditya.smartmedbox.feature.dashboard.domain.model.DeviceTelemetry
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveTelemetryUseCase
@Inject
constructor(
    private val repository: TelemetryRepository
) {
    operator fun invoke(deviceId: String): Flow<DeviceTelemetry> {
        return repository.observeTelemetry(deviceId)
    }
}
