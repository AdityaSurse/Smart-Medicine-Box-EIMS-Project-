package com.aditya.smartmedbox.feature.schedule.domain.usecase

import com.aditya.smartmedbox.feature.history.domain.evaluator.DoseEvaluator
import com.aditya.smartmedbox.feature.history.domain.model.DoseEvent
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import com.aditya.smartmedbox.feature.medicines.domain.repository.MedicinesRepository
import com.aditya.smartmedbox.feature.schedule.domain.repository.DoseScheduleRepository
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetTodayScheduleUseCase @Inject constructor(
    private val doseScheduleRepository: DoseScheduleRepository,
    private val doseEventRepository: DoseEventRepository,
    private val medicinesRepository: MedicinesRepository,
    private val doseEvaluator: DoseEvaluator = DoseEvaluator()
) {
    operator fun invoke(currentTimeMillis: Long = System.currentTimeMillis()): Flow<List<DoseEvent>> {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val dayOfWeekIndex = when (Calendar.getInstance().apply { timeInMillis = currentTimeMillis }.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        return combine(
            doseScheduleRepository.getSchedules(),
            doseEventRepository.getEventsForDay(startOfDay, endOfDay),
            medicinesRepository.getMedicines()
        ) { schedules, loggedEvents, medicines ->
            val activeSchedulesToday = schedules.filter {
                it.isActive && it.daysOfWeek.contains(dayOfWeekIndex)
            }

            val medMap = medicines.associateBy { it.id }
            val existingEventsByScheduleTime = loggedEvents.associateBy { "${it.scheduleId}_${it.scheduledTimeEpochMillis}" }

            val resultList = mutableListOf<DoseEvent>()

            for (schedule in activeSchedulesToday) {
                val med = medMap[schedule.medicineId]
                for (timeStr in schedule.times) {
                    val scheduledEpoch = parseTimeToEpochToday(timeStr, startOfDay)
                    val key = "${schedule.id}_$scheduledEpoch"
                    val existing = existingEventsByScheduleTime[key]
                    if (existing != null) {
                        resultList.add(existing)
                    } else {
                        val evalResult = doseEvaluator.evaluate(
                            schedule = schedule,
                            scheduledDoseEpochMillis = scheduledEpoch,
                            expectedPillWeightGrams = med?.estimatedUnitWeightGrams ?: 0.5f,
                            boxEvents = emptyList(),
                            currentEvaluationTimeMillis = currentTimeMillis
                        )
                        resultList.add(
                            DoseEvent(
                                id = "evt_${schedule.id}_$scheduledEpoch",
                                scheduleId = schedule.id,
                                medicineId = schedule.medicineId,
                                medicineName = med?.name ?: schedule.medicineName,
                                dosage = med?.dosageDescription ?: "1 tablet",
                                scheduledTimeEpochMillis = scheduledEpoch,
                                status = evalResult.status,
                                bayLabel = schedule.compartmentBay
                            )
                        )
                    }
                }
            }

            resultList.sortedBy { it.scheduledTimeEpochMillis }
        }
    }

    private fun parseTimeToEpochToday(timeStr: String, startOfDayMillis: Long): Long {
        val parts = timeStr.trim().split(" ", ":")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val amPm = parts.getOrNull(2) ?: if (timeStr.contains("PM", ignoreCase = true)) "PM" else "AM"

        if (amPm.equals("PM", ignoreCase = true) && hour < 12) hour += 12
        if (amPm.equals("AM", ignoreCase = true) && hour == 12) hour = 0

        return startOfDayMillis + (hour * 3600 + minute * 60) * 1000L
    }
}
