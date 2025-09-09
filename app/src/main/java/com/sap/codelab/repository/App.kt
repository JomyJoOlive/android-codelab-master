package com.sap.codelab.repository

import android.app.Application
import com.sap.codelab.utils.notification.NotificationUtils

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.initialize(this)
        NotificationUtils.createNotificationChannel(this)
    }
}
