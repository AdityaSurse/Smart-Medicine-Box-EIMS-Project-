package com.aditya.smartmedbox.feature.history.domain.repository

import com.aditya.smartmedbox.feature.history.domain.model.AdherenceStats
import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getIntakeHistory(): Flow<List<IntakeLog>>
    fun getAdherenceStats(): Flow<AdherenceStats>
    suspend fun refreshHistory(): Result<Unit>
}
