package com.aditya.smartmedbox.feature.history.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetWeeklyMissedCountUseCase @Inject constructor(
    private val doseEventRepository: DoseEventRepository
) {
    operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Flow<Int> {
        val sevenDaysAgo = currentTimeMillis - 7L * 24 * 60 * 60 * 1000L
        return doseEventRepository.getMissedEventsCount(sevenDaysAgo)
    }
}
