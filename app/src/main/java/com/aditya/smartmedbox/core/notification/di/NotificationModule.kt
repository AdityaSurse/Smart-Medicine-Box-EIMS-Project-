package com.aditya.smartmedbox.core.notification.di

import com.aditya.smartmedbox.core.notification.DefaultNotificationPermissionChecker
import com.aditya.smartmedbox.core.notification.NotificationPermissionChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun bindNotificationPermissionChecker(checker: DefaultNotificationPermissionChecker): NotificationPermissionChecker
}
