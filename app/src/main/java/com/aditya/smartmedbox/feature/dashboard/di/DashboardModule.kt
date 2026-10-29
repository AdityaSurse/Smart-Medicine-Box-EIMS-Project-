package com.aditya.smartmedbox.feature.dashboard.di

import com.aditya.smartmedbox.feature.dashboard.data.repository.RoomDeviceStatusRepository
import com.aditya.smartmedbox.feature.dashboard.data.repository.RoomTelemetryRepository
import com.aditya.smartmedbox.feature.dashboard.domain.repository.DeviceStatusRepository
import com.aditya.smartmedbox.feature.dashboard.domain.repository.TelemetryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindTelemetryRepository(roomTelemetryRepository: RoomTelemetryRepository): TelemetryRepository

    @Binds
    @Singleton
    abstract fun bindDeviceStatusRepository(roomDeviceStatusRepository: RoomDeviceStatusRepository): DeviceStatusRepository
}
