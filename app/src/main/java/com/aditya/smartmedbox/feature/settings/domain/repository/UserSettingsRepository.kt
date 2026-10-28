package com.aditya.smartmedbox.feature.settings.domain.repository

import com.aditya.smartmedbox.feature.settings.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit>
    suspend fun updateTheme(isDarkMode: Boolean): Result<Unit>
    suspend fun updateSnoozeInterval(minutes: Int): Result<Unit>
    suspend fun updateLowStockThreshold(threshold: Int): Result<Unit>
}

