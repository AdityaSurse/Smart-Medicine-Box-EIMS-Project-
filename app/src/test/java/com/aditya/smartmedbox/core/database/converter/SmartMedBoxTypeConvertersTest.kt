package com.aditya.smartmedbox.core.database.converter

import com.aditya.smartmedbox.feature.history.domain.model.DoseStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartMedBoxTypeConvertersTest {

    private val converters = SmartMedBoxTypeConverters()

    @Test
    fun `fromStringList and toStringList convert correctly`() {
        val original = listOf("08:00 AM", "12:30 PM", "08:00 PM")
        val json = converters.fromStringList(original)
        val decoded = converters.toStringList(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `fromIntList and toIntList convert correctly`() {
        val original = listOf(1, 2, 3, 4, 5)
        val json = converters.fromIntList(original)
        val decoded = converters.toIntList(json)

        assertEquals(original, decoded)
    }

    @Test
    fun `fromDoseStatus and toDoseStatus convert correctly`() {
        assertEquals("TAKEN", converters.fromDoseStatus(DoseStatus.TAKEN))
        assertEquals(DoseStatus.TAKEN, converters.toDoseStatus("TAKEN"))
        assertEquals(DoseStatus.MISSED, converters.toDoseStatus("MISSED"))
        assertEquals(DoseStatus.PENDING, converters.toDoseStatus("PENDING"))
        assertEquals(DoseStatus.PENDING, converters.toDoseStatus(null))
        assertEquals(DoseStatus.PENDING, converters.toDoseStatus("UNKNOWN_STATUS"))
    }
}
