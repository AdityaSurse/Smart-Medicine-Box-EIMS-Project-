package com.aditya.smartmedbox.feature.schedule.domain.alarm

interface DoseAlarmScheduler {
    fun scheduleReminder(scheduleId: String, medicineName: String, dosage: String, doseEpochMillis: Long, gracePeriodMinutes: Int, repeatCount: Int = 0)

    fun cancelReminder(scheduleId: String)

    suspend fun rescheduleAllActiveReminders()

    fun canScheduleExactAlarms(): Boolean
}
