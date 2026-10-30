package com.aditya.smartmedbox.core.notification

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationActionReceiverInstrumentedTest {

    private lateinit var context: Context
    private lateinit var receiver: TestNotificationActionReceiver

    class TestNotificationActionReceiver : NotificationActionReceiver() {
        var markAsTakenCalled = false
        var markedEventId: String? = null
        var markedScheduleId: String? = null
        var canceledNotificationId: Int? = null

        var snoozeCalled = false
        var snoozedScheduleId: String? = null
        var snoozedMedicineName: String? = null

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
            val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return

            when (action) {
                ACTION_MARK_AS_TAKEN -> {
                    val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
                    markAsTakenCalled = true
                    markedEventId = eventId
                    markedScheduleId = scheduleId
                    canceledNotificationId = notificationId
                }
                ACTION_SNOOZE_DOSE -> {
                    snoozeCalled = true
                    snoozedScheduleId = scheduleId
                    snoozedMedicineName = intent.getStringExtra(EXTRA_MEDICINE_NAME)
                    canceledNotificationId = notificationId
                }
            }
        }
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        receiver = TestNotificationActionReceiver()
    }

    @Test
    fun onReceive_markAsTakenAction_extractsExtrasCorrectly() {
        val intent = Intent(NotificationActionReceiver.ACTION_MARK_AS_TAKEN).apply {
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, "sch_test_101")
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, "evt_test_202")
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, 4040)
        }

        receiver.onReceive(context, intent)

        assertTrue(receiver.markAsTakenCalled)
        assertEquals("evt_test_202", receiver.markedEventId)
        assertEquals("sch_test_101", receiver.markedScheduleId)
        assertEquals(4040, receiver.canceledNotificationId)
    }

    @Test
    fun onReceive_snoozeAction_extractsExtrasCorrectly() {
        val intent = Intent(NotificationActionReceiver.ACTION_SNOOZE_DOSE).apply {
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, "sch_test_303")
            putExtra(NotificationActionReceiver.EXTRA_MEDICINE_NAME, "Metformin")
            putExtra(NotificationActionReceiver.EXTRA_DOSAGE, "500 mg")
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, 5050)
            putExtra(NotificationActionReceiver.EXTRA_GRACE_PERIOD_MINUTES, 30)
            putExtra(NotificationActionReceiver.EXTRA_REPEAT_COUNT, 1)
        }

        receiver.onReceive(context, intent)

        assertTrue(receiver.snoozeCalled)
        assertEquals("sch_test_303", receiver.snoozedScheduleId)
        assertEquals("Metformin", receiver.snoozedMedicineName)
        assertEquals(5050, receiver.canceledNotificationId)
    }

    @Test
    fun onReceive_withNullAction_doesNothing() {
        val intent = Intent()

        receiver.onReceive(context, intent)

        assertTrue(!receiver.markAsTakenCalled && !receiver.snoozeCalled)
    }
}
