package com.aditya.smartmedbox.core.util

import timber.log.Timber

object LogUtils {
    fun initLogging(isDebug: Boolean) {
        if (isDebug) {
            Timber.plant(
                object : Timber.DebugTree() {
                    override fun createStackElementTag(element: StackTraceElement): String {
                        val tag = super.createStackElementTag(element)
                        return "SmartMedBox:[$tag#${element.methodName}:${element.lineNumber}]"
                    }
                }
            )
        }
    }
}
