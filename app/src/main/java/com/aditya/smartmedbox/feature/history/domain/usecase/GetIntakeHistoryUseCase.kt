package com.aditya.smartmedbox.feature.history.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class IntakeHistorySummary(
    val events: List<DoseEvent>,
    val totalLogged: Int,
    val takenCount: Int,
    val missedCount: Int,
    val adherencePercentage: Int
)

class GetIntakeHistoryUseCase @Inject constructor(
    private val doseEventRepository: DoseEventRepository
) {
    operator fun invoke(): Flow<IntakeHistorySummary> {
        return doseEventRepository.getDoseEvents().map { events ->
            val sorted = events.sortedByDescending { it.scheduledTimeEpochMillis }
            val total = sorted.size
            val taken = sorted.count { it.status == DoseStatus.TAKEN }
            val missed = sorted.count { it.status == DoseStatus.MISSED }
            val adherence = if (total > 0) ((taken.toDouble() / total) * 100).toInt() else 100

            IntakeHistorySummary(
                events = sorted,
                totalLogged = total,
                takenCount = taken,
                missedCount = missed,
                adherencePercentage = adherence
            )
        }
    }
}

