package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.schedule.domain.usecase.GetTodayScheduleUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNextDoseUseCase @Inject constructor(
    private val getTodayScheduleUseCase: GetTodayScheduleUseCase
) {
    operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Flow<DoseEvent?> {
        return getTodayScheduleUseCase(currentTimeMillis).map { todayEvents ->
            todayEvents
                .filter { it.status == DoseStatus.PENDING && it.scheduledTimeEpochMillis >= currentTimeMillis - 15 * 60 * 1000L }
                .minByOrNull { it.scheduledTimeEpochMillis }
                ?: todayEvents.firstOrNull { it.status == DoseStatus.PENDING }
        }
    }
}
