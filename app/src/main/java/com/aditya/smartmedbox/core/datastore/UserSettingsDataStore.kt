package com.aditya.smartmedbox.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aditya.smartmedbox.feature.settings.domain.model.UserSettings
import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : UserSettingsRepository {

    private val dataStore = context.userPreferencesDataStore

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val SNOOZE_INTERVAL = intPreferencesKey("snooze_interval_minutes")
        val LOW_STOCK_THRESHOLD = intPreferencesKey("low_stock_threshold_default")
        val IS_CHIME_ENABLED = booleanPreferencesKey("is_chime_enabled")
        val CAREGIVER_NAME = stringPreferencesKey("primary_caregiver_name")
        val CAREGIVER_PHONE = stringPreferencesKey("primary_caregiver_phone")
    }

    override fun getUserSettings(): Flow<UserSettings> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserSettings(
                    userName = preferences[PreferencesKeys.USER_NAME] ?: "Eleanor Vance",
                    isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                    snoozeIntervalMinutes = preferences[PreferencesKeys.SNOOZE_INTERVAL] ?: 10,
                    lowStockThresholdDefault = preferences[PreferencesKeys.LOW_STOCK_THRESHOLD] ?: 5,
                    isChimeEnabled = preferences[PreferencesKeys.IS_CHIME_ENABLED] ?: true,
                    primaryCaregiverName = preferences[PreferencesKeys.CAREGIVER_NAME] ?: "Sarah Jenkins",
                    primaryCaregiverPhone = preferences[PreferencesKeys.CAREGIVER_PHONE] ?: "+1 (555) 234-5678"
                )
            }
    }

    override suspend fun updateUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.USER_NAME] = settings.userName
                preferences[PreferencesKeys.IS_DARK_MODE] = settings.isDarkMode
                preferences[PreferencesKeys.SNOOZE_INTERVAL] = settings.snoozeIntervalMinutes
                preferences[PreferencesKeys.LOW_STOCK_THRESHOLD] = settings.lowStockThresholdDefault
                preferences[PreferencesKeys.IS_CHIME_ENABLED] = settings.isChimeEnabled
                preferences[PreferencesKeys.CAREGIVER_NAME] = settings.primaryCaregiverName
                preferences[PreferencesKeys.CAREGIVER_PHONE] = settings.primaryCaregiverPhone
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTheme(isDarkMode: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSnoozeInterval(minutes: Int): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.SNOOZE_INTERVAL] = minutes
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateLowStockThreshold(threshold: Int): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.LOW_STOCK_THRESHOLD] = threshold
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

