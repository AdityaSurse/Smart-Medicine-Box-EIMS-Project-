package com.aditya.smartmedbox.feature.schedule.data.repository

import com.aditya.smartmedbox.feature.schedule.domain.model.PrescriptionSchedule
import com.aditya.smartmedbox.feature.schedule.domain.model.ScheduleDay
import com.aditya.smartmedbox.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeScheduleRepository @Inject constructor() : ScheduleRepository {

    private val daysState = MutableStateFlow(
        listOf(
            ScheduleDay(dayLetter = "M", dayNumber = 14, isToday = false, isSelected = false),
            ScheduleDay(dayLetter = "T", dayNumber = 15, isToday = false, isSelected = false),
            ScheduleDay(dayLetter = "W", dayNumber = 16, isToday = true, isSelected = true),
            ScheduleDay(dayLetter = "T", dayNumber = 17, isToday = false, isSelected = false),
            ScheduleDay(dayLetter = "F", dayNumber = 18, isToday = false, isSelected = false),
            ScheduleDay(dayLetter = "S", dayNumber = 19, isToday = false, isSelected = false),
            ScheduleDay(dayLetter = "S", dayNumber = 20, isToday = false, isSelected = false)
        )
    )

    private val schedulesState = MutableStateFlow(
        listOf(
            PrescriptionSchedule(
                id = "sch_amox_01",
                medicineName = "Amoxicillin",
                dosageStrength = "500 mg",
                formFactor = "Antibiotic capsule",
                trayBay = "Tray 01",
                dispenseTimes = listOf("8:00 AM", "8:00 PM"),
                daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S"),
                status = "Dispensing",
                scheduledHour = 8,
                scheduledMinute = 0,
                targetWeightDeltaGrams = 0.55f,
                totalSupply = 28,
                isActive = true
            ),
            PrescriptionSchedule(
                id = "sch_metf_02",
                medicineName = "Metformin",
                dosageStrength = "850 mg",
                formFactor = "Blood sugar regulation",
                trayBay = "Tray 02",
                dispenseTimes = listOf("12:30 PM"),
                daysOfWeek = listOf("M", "T", "W", "T", "F"),
                status = "Dispensing",
                scheduledHour = 12,
                scheduledMinute = 30,
                targetWeightDeltaGrams = 0.85f,
                totalSupply = 60,
                isActive = true
            )
        )
    )

    override fun getScheduleDays(): Flow<List<ScheduleDay>> = daysState.asStateFlow()

    override fun getSchedulesForDay(dayNumber: Int): Flow<List<PrescriptionSchedule>> {
        return schedulesState.map { list ->
            if (dayNumber == 19 || dayNumber == 20) {
                // Weekend may have only daily courses
                list.filter { it.daysOfWeek.contains("S") }
            } else {
                list
            }
        }
    }

    override suspend fun toggleScheduleActive(scheduleId: String, isActive: Boolean): Result<Unit> {
        schedulesState.value = schedulesState.value.map {
            if (it.id == scheduleId) it.copy(isActive = isActive) else it
        }
        return Result.success(Unit)
    }

    override suspend fun addSchedule(schedule: PrescriptionSchedule): Result<Unit> {
        schedulesState.value = schedulesState.value + schedule
        return Result.success(Unit)
    }
}

