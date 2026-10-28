package com.aditya.smartmedbox.feature.history.di

import com.aditya.smartmedbox.feature.history.data.repository.RoomDoseEventRepository
import com.aditya.smartmedbox.feature.history.data.repository.RoomHistoryRepository
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.history.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HistoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        roomHistoryRepository: RoomHistoryRepository
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindDoseEventRepository(
        roomDoseEventRepository: RoomDoseEventRepository
    ): DoseEventRepository
}
