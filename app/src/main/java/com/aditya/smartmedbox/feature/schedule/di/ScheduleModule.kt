package com.aditya.smartmedbox.feature.schedule.di

import com.aditya.smartmedbox.feature.schedule.data.repository.FakeScheduleRepository
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
    abstract fun bindScheduleRepository(
        fakeScheduleRepository: FakeScheduleRepository
    ): ScheduleRepository
}
