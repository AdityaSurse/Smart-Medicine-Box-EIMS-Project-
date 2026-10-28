package com.aditya.smartmedbox.core.database.converter

import androidx.room.TypeConverter
import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SmartMedBoxTypeConverters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (_: Exception) {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return json.encodeToString(value ?: emptyList())
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Int>>(value)
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDoseStatus(status: DoseStatus?): String {
        return status?.name ?: DoseStatus.PENDING.name
    }

    @TypeConverter
    fun toDoseStatus(value: String?): DoseStatus {
        return try {
            if (value != null) DoseStatus.valueOf(value) else DoseStatus.PENDING
        } catch (_: Exception) {
            DoseStatus.PENDING
        }
    }
}

