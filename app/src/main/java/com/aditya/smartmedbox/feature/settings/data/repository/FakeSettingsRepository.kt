package com.aditya.smartmedbox.feature.settings.data.repository

import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    override suspend fun updateUnitSystem(unitSystem: String): Result<Unit> {
        settingsState.value = settingsState.value.copy(unitSystem = unitSystem)
        return Result.success(Unit)
    }

    override suspend fun updateGracePeriod(minutes: Int): Result<Unit> {
        settingsState.value = settingsState.value.copy(gracePeriodMinutes = minutes)
        return Result.success(Unit)
    }

    override suspend fun updateReminderRepeatCount(count: Int): Result<Unit> {
        settingsState.value = settingsState.value.copy(reminderRepeatCount = count)
        return Result.success(Unit)
    }

    override suspend fun updateQuietHours(enabled: Boolean): Result<Unit> {
        settingsState.value = settingsState.value.copy(quietHoursEnabled = enabled)
        return Result.success(Unit)
    }

    override suspend fun clearAllData(): Result<Unit> {
        settingsState.value = DeviceSettings()
        return Result.success(Unit)
    }
}
