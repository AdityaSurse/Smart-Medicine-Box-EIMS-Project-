package com.aditya.smartmedbox.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aditya.smartmedbox.MainActivity
import com.aditya.smartmedbox.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoseNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_DOSE_REMINDERS = "channel_dose_reminders"
        const val CHANNEL_MISSED_DOSES = "channel_missed_doses"
        const val CHANNEL_DEVICE_ALERTS = "channel_device_alerts"
        const val CHANNEL_LOW_STOCK = "channel_low_stock"

        const val URI_SCHEDULE = "smartmedbox://schedule"
        const val URI_HISTORY = "smartmedbox://history"
        const val URI_DEVICE = "smartmedbox://device"
        const val URI_MEDICINES = "smartmedbox://medicines"
        const val URI_ALERTS = "smartmedbox://alerts"

        const val EXTRA_DOSE_EVENT_ID = "extra_dose_event_id"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun initChannels() {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
            .build()

        // 1. Dose Reminder Channel (High)
        val reminderChannel = NotificationChannel(
            CHANNEL_DOSE_REMINDERS,
            "Dose Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Urgent alerts for scheduled medication times"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setSound(soundUri, audioAttributes)
        }

        // 2. Missed Dose Channel (High)
        val missedChannel = NotificationChannel(
            CHANNEL_MISSED_DOSES,
            "Missed Dose",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when scheduled medication grace period expires"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            setSound(soundUri, audioAttributes)
        }

        // 3. Device Alerts Channel (Default)
        val deviceAlertsChannel = NotificationChannel(
            CHANNEL_DEVICE_ALERTS,
            "Device Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Box connectivity, battery, and environmental alerts"
            enableVibration(true)
        }

        // 4. Low Stock Channel (Low)
        val lowStockChannel = NotificationChannel(
            CHANNEL_LOW_STOCK,
            "Low Stock",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Medicine inventory refill reminders"
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(missedChannel)
        notificationManager.createNotificationChannel(deviceAlertsChannel)
        notificationManager.createNotificationChannel(lowStockChannel)
    }

    fun showDoseReminderNotification(
        notificationId: Int,
        scheduleId: String,
        medicineName: String,
        dosage: String,
        repeatCount: Int = 0,
        gracePeriodMinutes: Int = 30,
        eventId: String? = null
    ) {
        val contentIntent = createDeepLinkPendingIntent(URI_SCHEDULE, notificationId, scheduleId)

        // Action 1: "Mark as taken"
        val markTakenIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_AS_TAKEN
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(NotificationActionReceiver.EXTRA_EVENT_ID, eventId ?: "evt_${scheduleId}_${System.currentTimeMillis()}")
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Snooze 10 min"
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE_DOSE
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_SCHEDULE_ID, scheduleId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICINE_NAME, medicineName)
            putExtra(NotificationActionReceiver.EXTRA_DOSAGE, dosage)
            putExtra(NotificationActionReceiver.EXTRA_GRACE_PERIOD_MINUTES, gracePeriodMinutes)
            putExtra(NotificationActionReceiver.EXTRA_REPEAT_COUNT, repeatCount)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val repeatNotice = if (repeatCount > 0) " (Reminder #$repeatCount)" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_DOSE_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for $medicineName$repeatNotice")
            .setContentText("Prescription: $dosage. Open your Smart Medicine Box tray to dispense.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Prescription: $dosage.\nOpen your Smart Medicine Box tray to dispense your scheduled medication.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(R.mipmap.ic_launcher, "Mark as taken", markTakenPendingIntent)
            .addAction(R.mipmap.ic_launcher, "Snooze 10 min", snoozePendingIntent)
            .build()

        notifySafe(notificationId, notification)
    }

    fun showMissedDoseNotification(notificationId: Int, scheduleId: String, medicineName: String, dosage: String) {
        val contentIntent = createDeepLinkPendingIntent(URI_HISTORY, notificationId, scheduleId)

        val notification = NotificationCompat.Builder(context, CHANNEL_MISSED_DOSES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Missed Medication: $medicineName")
            .setContentText("The grace period for $dosage has expired without box extraction.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("The grace period for $medicineName ($dosage) has expired without detected weight reduction in your Smart Medicine Box.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notifySafe(notificationId, notification)
    }

    fun showDeviceAlertNotification(notificationId: Int, title: String, message: String, deepLinkUri: String = URI_DEVICE) {
        val contentIntent = createDeepLinkPendingIntent(deepLinkUri, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_DEVICE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notifySafe(notificationId, notification)
    }

    fun showEnvironmentalAlertNotification(notificationId: Int, title: String, message: String, deepLinkUri: String = URI_DEVICE) {
        val contentIntent = createDeepLinkPendingIntent(deepLinkUri, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_DEVICE_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notifySafe(notificationId, notification)
    }

    fun showLowStockNotification(notificationId: Int, medicineName: String, stockRemaining: Int, deepLinkUri: String = URI_MEDICINES) {
        val contentIntent = createDeepLinkPendingIntent(deepLinkUri, notificationId)

        val notification = NotificationCompat.Builder(context, CHANNEL_LOW_STOCK)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Low Stock: $medicineName")
            .setContentText("Only $stockRemaining tablets remaining in tray. Tap to view prescriptions.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Only $stockRemaining doses remaining for $medicineName. Please refill your Smart Medicine Box compartment soon.")
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        notifySafe(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    private fun createDeepLinkPendingIntent(uriString: String, requestCode: Int, eventOrScheduleId: String? = null): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString), context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (eventOrScheduleId != null) {
                putExtra(EXTRA_DOSE_EVENT_ID, eventOrScheduleId)
            }
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun notifySafe(notificationId: Int, notification: android.app.Notification) {
        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user
        }
    }
}
