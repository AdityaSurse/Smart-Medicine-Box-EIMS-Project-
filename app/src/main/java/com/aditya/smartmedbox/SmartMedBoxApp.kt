package com.aditya.smartmedbox

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aditya.smartmedbox.core.notification.DoseNotificationManager
import com.aditya.smartmedbox.core.util.LogUtils
import com.aditya.smartmedbox.core.worker.DoseReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartMedBoxApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var notificationManager: DoseNotificationManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        LogUtils.initLogging(isDebug = BuildConfig.DEBUG)
        notificationManager.initChannels()
        DoseReminderWorker.enqueuePeriodicAudit(this)
    }
}
