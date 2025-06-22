package com.devilishtruthstare.scribereader

import android.app.Application
import com.devilishtruthstare.scribereader.settings.SettingsData

class ScribeReader: Application() {
    lateinit var settings: SettingsData
    override fun onCreate() {
        super.onCreate()
        settings = SettingsData().load(applicationContext)
    }
}