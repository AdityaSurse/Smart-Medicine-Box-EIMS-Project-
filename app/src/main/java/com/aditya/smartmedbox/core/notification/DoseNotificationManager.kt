package com.aditya.smartmedbox.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
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
        const val EXTRA_DOSE_EVENT_ID = "extra_dose_event_id"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun initChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val reminderChannel = NotificationChannel(
                CHANNEL_DOSE_REMINDERS,
                "Dose Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent alerts for scheduled medication times"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
            }

            val missedChannel = NotificationChannel(
                CHANNEL_MISSED_DOSES,
                "Missed Medication Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when a scheduled dose passes its grace window"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(soundUri, audioAttributes)
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(missedChannel)
        }
    }

    fun showDoseReminderNotification(notificationId: Int, scheduleId: String, medicineName: String, dosage: String, repeatCount: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DOSE_EVENT_ID, scheduleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val repeatNotice = if (repeatCount > 0) " (Reminder #$repeatCount)" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_DOSE_REMINDERS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Time for $medicineName$repeatNotice")
            .setContentText("Prescription: $dosage. Please open your Smart Medicine Box to dispense.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Prescription: $dosage.\nOpen your Smart Medicine Box tray to dispense your scheduled medication.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS might be denied by user
        }
    }

    fun showMissedDoseNotification(notificationId: Int, scheduleId: String, medicineName: String, dosage: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DOSE_EVENT_ID, scheduleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS might be denied by user
        }
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}
