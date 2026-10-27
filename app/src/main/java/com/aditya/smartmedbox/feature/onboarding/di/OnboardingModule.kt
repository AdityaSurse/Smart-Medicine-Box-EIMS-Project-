package com.aditya.smartmedbox.feature.onboarding.di

import com.aditya.smartmedbox.feature.onboarding.data.repository.FakeOnboardingRepository
import com.aditya.smartmedbox.feature.onboarding.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OnboardingModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: FakeOnboardingRepository): OnboardingRepository
}
