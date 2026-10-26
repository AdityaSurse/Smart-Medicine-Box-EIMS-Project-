package com.aditya.smartmedbox

import android.app.Application
import com.aditya.smartmedbox.core.util.LogUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartMedBoxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LogUtils.initLogging(isDebug = BuildConfig.DEBUG)
    }
}
