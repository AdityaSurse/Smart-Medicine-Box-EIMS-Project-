package com.aditya.smartmedbox.core.designsystem.component

import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar
import java.util.Locale

object SmartMedBoxTimePickerHelper {

    fun showTimePickerDialog(
        context: Context,
        initialTime: String? = null,
        onTimeSelected: (String) -> Unit
    ) {
        val (initialHour, initialMinute) = parseTime(initialTime)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val formatted = formatTime(hourOfDay, minute)
                onTimeSelected(formatted)
            },
            initialHour,
            initialMinute,
            false // 12-hour format with AM/PM toggle
        ).show()
    }

    fun getCurrentFormattedTime(offsetMinutes: Int = 0): String {
        val calendar = Calendar.getInstance()
        if (offsetMinutes != 0) {
            calendar.add(Calendar.MINUTE, offsetMinutes)
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return formatTime(hour, minute)
    }

    fun formatTime(hourOfDay: Int, minute: Int): String {
        val isPm = hourOfDay >= 12
        val hour12 = when {
            hourOfDay == 0 -> 12
            hourOfDay > 12 -> hourOfDay - 12
            else -> hourOfDay
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, if (isPm) "PM" else "AM")
    }

    fun parseTime(timeStr: String?): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        if (timeStr.isNullOrBlank()) {
            return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
        return try {
            val parts = timeStr.trim().split(":", " ")
            var hour = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.HOUR_OF_DAY)
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: cal.get(Calendar.MINUTE)
            val isPm = timeStr.contains("PM", ignoreCase = true)
            val isAm = timeStr.contains("AM", ignoreCase = true)

            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0

            Pair(hour, minute)
        } catch (_: Exception) {
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
    }
}
