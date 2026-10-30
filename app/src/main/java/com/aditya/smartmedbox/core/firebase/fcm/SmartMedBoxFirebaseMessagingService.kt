package com.aditya.smartmedbox.core.firebase.fcm

import com.aditya.smartmedbox.core.notification.DoseNotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class SmartMedBoxFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: DoseNotificationManager

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM registration token received: $token")
        fcmTokenManager.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        Timber.d("FCM payload received with data: $data")
        if (data.isEmpty()) return

        val type = data["type"] ?: return
        when (type) {
            "DOSE_REMINDER" -> handleDoseReminder(data)
            "MISSED_DOSE" -> handleMissedDose(data)
            "DEVICE_OFFLINE" -> handleDeviceOffline(data)
            "ENVIRONMENTAL_ALERT" -> handleEnvironmentalAlert(data)
            "LOW_STOCK" -> handleLowStock(data)
            else -> Timber.d("Unrecognized FCM message type: $type")
        }
    }

    private fun handleDoseReminder(data: Map<String, String>) {
        val scheduleId = data["scheduleId"] ?: "fcm_dose"
        val medicineName = data["medicineName"] ?: "Medication"
        val dosage = data["dosage"] ?: "1 dose"
        val repeatCount = data["repeatCount"]?.toIntOrNull() ?: 0
        val gracePeriod = data["gracePeriodMinutes"]?.toIntOrNull() ?: 30
        val notificationId = (scheduleId + "reminder").hashCode()

        notificationManager.showDoseReminderNotification(
            notificationId = notificationId,
            scheduleId = scheduleId,
            medicineName = medicineName,
            dosage = dosage,
            repeatCount = repeatCount,
            gracePeriodMinutes = gracePeriod
        )
    }

    private fun handleMissedDose(data: Map<String, String>) {
        val scheduleId = data["scheduleId"] ?: "fcm_missed"
        val medicineName = data["medicineName"] ?: "Medication"
        val dosage = data["dosage"] ?: "1 dose"
        val notificationId = (scheduleId + "missed").hashCode()

        notificationManager.showMissedDoseNotification(
            notificationId = notificationId,
            scheduleId = scheduleId,
            medicineName = medicineName,
            dosage = dosage
        )
    }

    private fun handleDeviceOffline(data: Map<String, String>) {
        val deviceId = data["deviceId"] ?: "Smart Box #DG-4081"
        val offlineMinutes = data["offlineMinutes"]?.toIntOrNull() ?: 30
        val title = data["title"] ?: "Smart Medicine Box Offline"
        val message = data["message"] ?: "Device $deviceId has been unreachable for over $offlineMinutes minutes."

        notificationManager.showDeviceAlertNotification(
            notificationId = "device_offline".hashCode(),
            title = title,
            message = message
        )
    }

    private fun handleEnvironmentalAlert(data: Map<String, String>) {
        val title = data["title"] ?: "Chamber Environmental Warning"
        val message = data["message"] ?: "Box temperature or humidity readings are outside safe medication boundaries."

        notificationManager.showEnvironmentalAlertNotification(
            notificationId = "environmental_alert".hashCode(),
            title = title,
            message = message
        )
    }

    private fun handleLowStock(data: Map<String, String>) {
        val medicineName = data["medicineName"] ?: "Medication"
        val remaining = data["remaining"]?.toIntOrNull() ?: 3

        notificationManager.showLowStockNotification(
            notificationId = (medicineName + "low_stock").hashCode(),
            medicineName = medicineName,
            stockRemaining = remaining
        )
    }
}
