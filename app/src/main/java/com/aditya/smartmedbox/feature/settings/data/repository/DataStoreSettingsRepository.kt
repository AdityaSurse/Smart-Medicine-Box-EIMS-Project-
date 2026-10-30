package com.aditya.smartmedbox.feature.settings.data.repository

import com.aditya.smartmedbox.core.database.SmartMedBoxDatabase
import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.feature.settings.domain.model.CaregiverContact
import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val deviceStatusDao: DeviceStatusDao,
    private val database: SmartMedBoxDatabase
) : SettingsRepository {

    override fun getSettings(): Flow<DeviceSettings> {
        return combine(
            userSettingsRepository.getUserSettings(),
            deviceStatusDao.getDeviceStatusFlow("DG_BOX_4081")
        ) { userSettings, deviceStatus ->
            val battery = deviceStatus?.batteryPercent ?: 84
            val isOnline = deviceStatus?.isOnline ?: true

            DeviceSettings(
                deviceName = "DoseGuard Bay 4X",
                hardwareId = "HW #04",
                serialNumber = "#DG-4081-B",
                firmwareVersion = "v2.4.1",
                isOnline = isOnline,
                lastSyncTime = "Just now",
                batteryPercent = battery,
                wifiSsid = "Apothecary_Guest_5G",
                soundChimeName = if (userSettings.isChimeEnabled) "Gentle Chime (65dB)" else "Muted",
                snoozeIntervalMinutes = userSettings.snoozeIntervalMinutes,
                refillThresholdTablets = userSettings.lowStockThresholdDefault,
                isDarkMode = userSettings.isDarkMode,
                unitSystem = userSettings.unitSystem,
                gracePeriodMinutes = userSettings.gracePeriodMinutes,
                reminderRepeatCount = userSettings.maxReminderRepeats,
                quietHoursEnabled = userSettings.quietHoursEnabled,
                quietHoursText = userSettings.quietHoursText,
                caregivers = listOf(
                    CaregiverContact(
                        id = "cg_01",
                        name = userSettings.primaryCaregiverName,
                        phoneNumber = userSettings.primaryCaregiverPhone,
                        isAlertsEnabled = true,
                        initials = userSettings.primaryCaregiverName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
                    )
                )
            )
        }
    }

    override suspend fun updateSnoozeInterval(minutes: Int): Result<Unit> {
        return userSettingsRepository.updateSnoozeInterval(minutes)
    }

    override suspend fun updateRefillThreshold(tablets: Int): Result<Unit> {
        return userSettingsRepository.updateLowStockThreshold(tablets)
    }

    override suspend fun updateThemeMode(isDark: Boolean): Result<Unit> {
        return userSettingsRepository.updateTheme(isDark)
    }

    override suspend fun playTestChime(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun updateUnitSystem(unitSystem: String): Result<Unit> {
        return userSettingsRepository.updateUnitSystem(unitSystem)
    }

    override suspend fun updateGracePeriod(minutes: Int): Result<Unit> {
        return userSettingsRepository.updateGracePeriod(minutes)
    }

    override suspend fun updateReminderRepeatCount(count: Int): Result<Unit> {
        return userSettingsRepository.updateMaxReminderRepeats(count)
    }

    override suspend fun updateQuietHours(enabled: Boolean): Result<Unit> {
        return userSettingsRepository.updateQuietHours(enabled)
    }

    override suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            database.clearAllTables()
            userSettingsRepository.clearAllData()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
