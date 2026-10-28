package com.aditya.smartmedbox.feature.settings.data.repository

import com.aditya.smartmedbox.core.database.dao.DeviceStatusDao
import com.aditya.smartmedbox.feature.settings.domain.model.CaregiverContact
import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import com.aditya.smartmedbox.feature.settings.domain.model.HardwareBayStatus
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
    private val deviceStatusDao: DeviceStatusDao
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
}
