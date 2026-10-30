package com.aditya.smartmedbox.feature.device.domain.usecase

import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject

class UnpairDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String): Result<Unit> {
        if (deviceId.isBlank()) {
            return Result.failure(IllegalArgumentException("Device ID cannot be empty"))
        }
        return try {
            deviceRepository.unpairDevice(deviceId.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
