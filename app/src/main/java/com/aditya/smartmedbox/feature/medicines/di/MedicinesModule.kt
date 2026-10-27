package com.aditya.smartmedbox.feature.medicines.di

import com.aditya.smartmedbox.feature.medicines.data.repository.FakeMedicinesRepository
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MedicinesModule {

    @Binds
    @Singleton
    abstract fun bindMedicinesRepository(
        fakeMedicinesRepository: FakeMedicinesRepository
    ): MedicinesRepository
}
