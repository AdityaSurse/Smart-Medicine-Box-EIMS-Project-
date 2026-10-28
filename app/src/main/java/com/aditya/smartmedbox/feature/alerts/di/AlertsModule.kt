package com.aditya.smartmedbox.feature.alerts.di

import com.aditya.smartmedbox.feature.alerts.data.repository.FakeAlertsRepository
import com.aditya.smartmedbox.feature.alerts.domain.repository.AlertsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlertsModule {

    @Binds
    @Singleton
    abstract fun bindAlertsRepository(
        fakeAlertsRepository: FakeAlertsRepository
    ): AlertsRepository
}

