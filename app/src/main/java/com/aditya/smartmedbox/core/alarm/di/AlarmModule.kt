package com.aditya.smartmedbox.core.alarm.di

import com.aditya.smartmedbox.core.alarm.AndroidDoseAlarmScheduler
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindDoseAlarmScheduler(androidDoseAlarmScheduler: AndroidDoseAlarmScheduler): DoseAlarmScheduler
}
