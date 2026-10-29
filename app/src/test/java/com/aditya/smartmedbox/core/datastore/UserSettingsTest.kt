package com.aditya.smartmedbox.core.datastore

import com.aditya.smartmedbox.feature.settings.domain.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserSettingsTest {

    @Test
    fun `default user settings have expected apothecary values`() {
        val settings = UserSettings()

        assertEquals("Eleanor Vance", settings.userName)
        assertFalse(settings.isDarkMode)
        assertEquals(10, settings.snoozeIntervalMinutes)
        assertEquals(3, settings.maxReminderRepeats)
        assertEquals(5, settings.lowStockThresholdDefault)
        assertTrue(settings.isChimeEnabled)
        assertEquals("Sarah Jenkins", settings.primaryCaregiverName)
        assertEquals("+1 (555) 234-5678", settings.primaryCaregiverPhone)
    }

    @Test
    fun `copying user settings updates theme and snooze interval correctly`() {
        val initial = UserSettings()
        val updated = initial.copy(
            isDarkMode = true,
            snoozeIntervalMinutes = 15,
            maxReminderRepeats = 5,
            lowStockThresholdDefault = 8
        )

        assertTrue(updated.isDarkMode)
        assertEquals(15, updated.snoozeIntervalMinutes)
        assertEquals(5, updated.maxReminderRepeats)
        assertEquals(8, updated.lowStockThresholdDefault)
        assertEquals(initial.userName, updated.userName)
    }
}
