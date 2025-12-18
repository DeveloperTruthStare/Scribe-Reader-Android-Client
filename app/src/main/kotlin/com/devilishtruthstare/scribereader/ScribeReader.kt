package com.devilishtruthstare.scribereader

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.devilishtruthstare.scribereader.settings.SettingsData

class ScribeReader: Application() {
    lateinit var settings: SettingsData
    override fun onCreate() {
        super.onCreate()
        settings = SettingsData().load(applicationContext)

        val channel = NotificationChannel(
            "my_channel_id",
            "My Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}