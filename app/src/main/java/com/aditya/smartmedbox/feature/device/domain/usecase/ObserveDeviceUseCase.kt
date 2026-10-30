package com.aditya.smartmedbox.feature.device.domain.usecase

import com.aditya.smartmedbox.feature.device.domain.model.SmartBoxDevice
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<SmartBoxDevice?> {
        return deviceRepository.observeDiscoveredDevice()
    }
}
