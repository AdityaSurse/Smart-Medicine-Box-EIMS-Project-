package com.aditya.smartmedbox.feature.settings.domain.repository

import com.aditya.smartmedbox.feature.settings.domain.model.DeviceSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<DeviceSettings>
    suspend fun updateSnoozeInterval(minutes: Int): Result<Unit>
    suspend fun updateRefillThreshold(tablets: Int): Result<Unit>
    suspend fun updateThemeMode(isDark: Boolean): Result<Unit>
    suspend fun playTestChime(): Result<Unit>
}

