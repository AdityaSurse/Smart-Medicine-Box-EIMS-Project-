package com.aditya.smartmedbox.feature.schedule.di

import com.aditya.smartmedbox.feature.schedule.data.repository.RoomDoseScheduleRepository
import com.aditya.smartmedbox.feature.schedule.data.repository.RoomScheduleRepository
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import com.aditya.smartmedbox.feature.schedule.domain.repository.ScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleModule {

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(roomScheduleRepository: RoomScheduleRepository): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindDoseScheduleRepository(roomDoseScheduleRepository: RoomDoseScheduleRepository): DoseScheduleRepository
}
