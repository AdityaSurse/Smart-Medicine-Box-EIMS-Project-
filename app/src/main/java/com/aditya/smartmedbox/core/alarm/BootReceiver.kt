package com.aditya.smartmedbox.core.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditya.smartmedbox.core.worker.DoseReminderWorker
import com.aditya.smartmedbox.feature.schedule.domain.alarm.DoseAlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: DoseAlarmScheduler

    companion object {
        private val SUPPORTED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in SUPPORTED_ACTIONS) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                alarmScheduler.rescheduleAllActiveReminders()
                DoseReminderWorker.enqueueImmediateAudit(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
