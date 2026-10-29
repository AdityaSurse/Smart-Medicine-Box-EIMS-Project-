package com.aditya.smartmedbox.core.datastore

import com.aditya.smartmedbox.feature.settings.domain.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataStoreModule {

    @Binds
    @Singleton
    abstract fun bindUserSettingsRepository(userSettingsDataStore: UserSettingsDataStore): UserSettingsRepository
}
