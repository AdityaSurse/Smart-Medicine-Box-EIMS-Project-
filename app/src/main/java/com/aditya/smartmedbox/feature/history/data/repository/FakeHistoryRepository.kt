package com.aditya.smartmedbox.feature.history.data.repository

import com.aditya.smartmedbox.core.designsystem.component.DoseStatus
import com.aditya.smartmedbox.feature.history.domain.model.AdherenceStats
import com.aditya.smartmedbox.feature.history.domain.model.IntakeLog
import com.aditya.smartmedbox.feature.history.domain.repository.HistoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class FakeHistoryRepository @Inject constructor() : HistoryRepository {

    private val defaultLogs = listOf(
        IntakeLog(
            id = "log_1",
            medicineName = "Levothyroxine",
            dosageInstructions = "75 mcg • 1 Tablet",
            timeAndBay = "8:00 AM • Bay #1",
            dateGroup = "Today — Oct 16",
            timestamp = System.currentTimeMillis() - 3600_000 * 4,
            status = DoseStatus.TAKEN
        ),
        IntakeLog(
            id = "log_2",
            medicineName = "Vitamin D3",
            dosageInstructions = "2000 IU • Softgel",
            timeAndBay = "1:30 PM • Bay #3",
            dateGroup = "Today — Oct 16",
            timestamp = System.currentTimeMillis() - 3600_000 * 2,
            status = DoseStatus.TAKEN
        ),
        IntakeLog(
            id = "log_3",
            medicineName = "Paracetamol",
            dosageInstructions = "650 mg • Evening Dose",
            timeAndBay = "8:00 PM • Bay #4",
            dateGroup = "Yesterday — Oct 15",
            timestamp = System.currentTimeMillis() - 86400_000,
            status = DoseStatus.MISSED
        ),
        IntakeLog(
            id = "log_4",
            medicineName = "Levothyroxine",
            dosageInstructions = "75 mcg • 1 Tablet",
            timeAndBay = "8:00 AM • Bay #1",
            dateGroup = "Yesterday — Oct 15",
            timestamp = System.currentTimeMillis() - 86400_000 - 3600_000 * 12,
            status = DoseStatus.TAKEN
        ),
        IntakeLog(
            id = "log_5",
            medicineName = "Amoxicillin",
            dosageInstructions = "500 mg • Course Day 5",
            timeAndBay = "8:00 PM • Bay #2",
            dateGroup = "Oct 14",
            timestamp = System.currentTimeMillis() - 86400_000 * 2,
            status = DoseStatus.TAKEN
        )
    )

    private val _logsFlow = MutableStateFlow(defaultLogs)
    private val _statsFlow = MutableStateFlow(AdherenceStats())

    override fun getIntakeHistory(): Flow<List<IntakeLog>> = _logsFlow.asStateFlow()

    override fun getAdherenceStats(): Flow<AdherenceStats> = _statsFlow.asStateFlow()

    override suspend fun refreshHistory(): Result<Unit> {
        delay(500)
        _logsFlow.value = defaultLogs
        _statsFlow.value = AdherenceStats()
        return Result.success(Unit)
    }
}
