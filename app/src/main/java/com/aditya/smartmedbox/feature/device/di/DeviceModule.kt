package com.aditya.smartmedbox.feature.device.di

import com.aditya.smartmedbox.feature.device.data.provisioning.DefaultWifiProvisioner
import com.aditya.smartmedbox.feature.device.data.repository.RoomDeviceRepository
import com.aditya.smartmedbox.feature.device.domain.provisioning.WifiProvisioner
import com.aditya.smartmedbox.feature.device.domain.repository.DeviceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceModule {

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(roomDeviceRepository: RoomDeviceRepository): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindWifiProvisioner(defaultWifiProvisioner: DefaultWifiProvisioner): WifiProvisioner
}
