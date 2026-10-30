package com.aditya.smartmedbox.core.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

interface NotificationPermissionChecker {
    fun hasNotificationPermission(context: Context): Boolean
}

@Singleton
class DefaultNotificationPermissionChecker @Inject constructor() : NotificationPermissionChecker {
    override fun hasNotificationPermission(context: Context): Boolean {
        return NotificationPermissionHelper.hasNotificationPermission(context)
    }
}

object NotificationPermissionHelper {

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
