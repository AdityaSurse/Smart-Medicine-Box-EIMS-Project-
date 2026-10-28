package com.aditya.smartmedbox.feature.settings.data.repository

import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSettingsRepository @Inject constructor() : SettingsRepository {

    private val settingsState = MutableStateFlow(DeviceSettings())

    override fun getSettings(): Flow<DeviceSettings> = settingsState.asStateFlow()

    override suspend fun updateSnoozeInterval(minutes: Int): Result<Unit> {
        settingsState.value = settingsState.value.copy(snoozeIntervalMinutes = minutes)
        return Result.success(Unit)
    }

    override suspend fun updateRefillThreshold(tablets: Int): Result<Unit> {
        settingsState.value = settingsState.value.copy(refillThresholdTablets = tablets)
        return Result.success(Unit)
    }

    override suspend fun updateThemeMode(isDark: Boolean): Result<Unit> {
        settingsState.value = settingsState.value.copy(isDarkMode = isDark)
        return Result.success(Unit)
    }

    override suspend fun playTestChime(): Result<Unit> {
        return Result.success(Unit)
    }
}

