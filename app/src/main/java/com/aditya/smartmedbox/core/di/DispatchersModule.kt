package com.aditya.smartmedbox.core.di

import com.aditya.smartmedbox.core.util.DefaultDispatcherProvider
import com.aditya.smartmedbox.core.util.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(dispatchers: DispatcherProvider): CoroutineDispatcher = dispatchers.io

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(dispatchers: DispatcherProvider): CoroutineDispatcher = dispatchers.main

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(dispatchers: DispatcherProvider): CoroutineDispatcher = dispatchers.default
}
