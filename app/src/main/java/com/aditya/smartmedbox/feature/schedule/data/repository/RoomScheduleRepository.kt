package com.aditya.smartmedbox.feature.schedule.data.repository

import com.aditya.smartmedbox.core.database.dao.DoseScheduleDao
import com.aditya.smartmedbox.core.database.dao.MedicineDao
import com.aditya.smartmedbox.core.database.entity.DoseScheduleEntity
import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay
import com.aditya.smartmedbox.feature.schedule.domain.repository.ScheduleRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class RoomScheduleRepository @Inject constructor(
    private val doseScheduleDao: DoseScheduleDao,
    private val medicineDao: MedicineDao
) : ScheduleRepository {

    override fun getScheduleDays(): Flow<List<ScheduleDay>> {
        val calendar = Calendar.getInstance()
        val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Generate 7-day strip centered around today
        val days = mutableListOf<ScheduleDay>()
        val calClone = calendar.clone() as Calendar
        calClone.add(Calendar.DAY_OF_MONTH, -3)

        val dayLetters = mapOf(
            Calendar.MONDAY to "M",
            Calendar.TUESDAY to "T",
            Calendar.WEDNESDAY to "W",
            Calendar.THURSDAY to "T",
            Calendar.FRIDAY to "F",
            Calendar.SATURDAY to "S",
            Calendar.SUNDAY to "S"
        )

        repeat(7) {
            val dNum = calClone.get(Calendar.DAY_OF_MONTH)
            val dWeek = calClone.get(Calendar.DAY_OF_WEEK)
            val isToday = dNum == currentDayOfMonth
            days.add(
                ScheduleDay(
                    dayLetter = dayLetters[dWeek] ?: "M",
                    dayNumber = dNum,
                    isToday = isToday,
                    isSelected = isToday
                )
            )
            calClone.add(Calendar.DAY_OF_MONTH, 1)
        }

        return flowOf(days)
    }

    override fun getSchedulesForDay(dayNumber: Int): Flow<List<PrescriptionSchedule>> {
        return combine(
            doseScheduleDao.getAllSchedules(),
            medicineDao.getAllMedicines()
        ) { schedules, medicines ->
            val medicineMap = medicines.associateBy { it.id }

            schedules.map { entity ->
                val med = medicineMap[entity.medicineId]
                val firstTime = entity.times.firstOrNull() ?: "08:00 AM"
                val hour = parseHour(firstTime)
                val minute = parseMinute(firstTime)

                val dayLetters = entity.daysOfWeek.map { index ->
                    when (index) {
                        1 -> "M"
                        2 -> "T"
                        3 -> "W"
                        4 -> "T"
                        5 -> "F"
                        6 -> "S"
                        7 -> "S"
                        else -> "M"
                    }
                }

                PrescriptionSchedule(
                    id = entity.id,
                    medicineName = entity.medicineName,
                    dosageStrength = med?.let { "${it.strengthMg} mg" } ?: "500 mg",
                    formFactor = med?.formFactor ?: "Capsule",
                    trayBay = entity.compartmentBay,
                    dispenseTimes = entity.times,
                    daysOfWeek = dayLetters,
                    status = if (entity.isActive) "Active" else "Paused",
                    scheduledHour = hour,
                    scheduledMinute = minute,
                    targetWeightDeltaGrams = med?.estimatedUnitWeightGrams ?: 0.5f,
                    totalSupply = med?.currentStock ?: 30,
                    isActive = entity.isActive
                )
            }
        }
    }

    override suspend fun toggleScheduleActive(scheduleId: String, isActive: Boolean): Result<Unit> {
        return try {
            doseScheduleDao.toggleActive(scheduleId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addSchedule(schedule: PrescriptionSchedule): Result<Unit> {
        return try {
            val entity = DoseScheduleEntity(
                id = schedule.id,
                medicineId = "med_custom_${System.currentTimeMillis()}",
                medicineName = schedule.medicineName,
                times = schedule.dispenseTimes,
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7),
                gracePeriodMinutes = 30,
                compartmentBay = schedule.trayBay,
                isActive = schedule.isActive
            )
            doseScheduleDao.upsertSchedule(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseHour(time: String): Int {
        val parts = time.trim().split(":", " ")
        var hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val isPm = time.contains("PM", ignoreCase = true)
        if (isPm && hour < 12) hour += 12
        if (!isPm && hour == 12) hour = 0
        return hour
    }

    private fun parseMinute(time: String): Int {
        val parts = time.trim().split(":", " ")
        return parts.getOrNull(1)?.toIntOrNull() ?: 0
    }
}
