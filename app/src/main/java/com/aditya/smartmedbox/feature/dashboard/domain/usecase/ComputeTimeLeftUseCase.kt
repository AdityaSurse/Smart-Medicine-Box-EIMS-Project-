package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import javax.inject.Inject
import kotlin.math.abs

class ComputeTimeLeftUseCase @Inject constructor() {
    operator fun invoke(scheduledTimeMillis: Long, currentTimeMillis: Long = System.currentTimeMillis()): String {
        val diffMillis = scheduledTimeMillis - currentTimeMillis
        val diffMinutes = diffMillis / (1000 * 60)

        return when {
            diffMinutes in -2..2 -> "Due now"
            diffMinutes > 0 -> {
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                if (hours > 0) {
                    if (mins > 0) "in ${hours}h ${mins}m" else "in ${hours}h"
                } else {
                    "in ${mins}m"
                }
            }
            else -> {
                val overdueMins = abs(diffMinutes)
                val hours = overdueMins / 60
                val mins = overdueMins % 60
                if (hours > 0) {
                    "${hours}h ${mins}m overdue"
                } else {
                    "${mins}m overdue"
                }
            }
        }
    }
}
