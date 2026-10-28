package com.aditya.smartmedbox.feature.settings.di

import com.aditya.smartmedbox.feature.settings.data.repository.FakeSettingsRepository
import com.aditya.smartmedbox.feature.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        fakeSettingsRepository: FakeSettingsRepository
    ): SettingsRepository
}

