package com.aditya.smartmedbox.feature.device.domain.usecase

import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import javax.inject.Inject

class PairDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(deviceId: String, pairingToken: String): Result<Unit> {
        if (deviceId.isBlank()) {
            return Result.failure(IllegalArgumentException("Device ID cannot be empty"))
        }
        return try {
            deviceRepository.pairDevice(deviceId.trim(), pairingToken.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
