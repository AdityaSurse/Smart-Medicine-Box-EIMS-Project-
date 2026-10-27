package com.aditya.smartmedbox.feature.onboarding.domain.repository

import com.aditya.smartmedbox.feature.onboarding.domain.model.OnboardingDevice
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun getDiscoveredDevice(): Flow<OnboardingDevice?>
    suspend fun pairDevice(code: String): Result<Boolean>
    suspend fun resetPairing(): Result<Unit>
}
