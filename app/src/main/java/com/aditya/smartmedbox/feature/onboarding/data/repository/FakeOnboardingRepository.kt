package com.aditya.smartmedbox.feature.onboarding.data.repository

import com.aditya.smartmedbox.feature.onboarding.domain.model.OnboardingDevice
import com.aditya.smartmedbox.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeOnboardingRepository @Inject constructor() : OnboardingRepository {
    private val _deviceFlow = MutableStateFlow<OnboardingDevice?>(OnboardingDevice())

    override fun getDiscoveredDevice(): Flow<OnboardingDevice?> = _deviceFlow.asStateFlow()

    override suspend fun pairDevice(code: String): Result<Boolean> {
        delay(600) // simulate BLE handshake
        val current = _deviceFlow.value
        return if (current != null && (code == current.pairingCode || code.isEmpty())) {
            _deviceFlow.value = current.copy(isPaired = true)
            Result.success(true)
        } else {
            Result.failure(IllegalArgumentException("Invalid pairing code"))
        }
    }

    override suspend fun resetPairing(): Result<Unit> {
        _deviceFlow.value = OnboardingDevice(isPaired = false)
        return Result.success(Unit)
    }
}
