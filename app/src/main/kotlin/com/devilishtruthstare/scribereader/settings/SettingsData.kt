package com.devilishtruthstare.scribereader.settings

import android.content.Context
import android.content.SharedPreferences

data class SettingsData (
    var verticalText: Boolean = true,
    var showLearning: Boolean = true,
    var playTTS: Boolean = true
) {
    private lateinit var prefs: SharedPreferences
    fun save() {
        prefs.edit().putBoolean("vertical_text", verticalText).apply()
        prefs.edit().putBoolean("learning_mode", showLearning).apply()
        prefs.edit().putBoolean("tts_mode", playTTS).apply()
    }

    fun load(context: Context): SettingsData {
        prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        verticalText = prefs.getBoolean("vertical_text", true)
        showLearning = prefs.getBoolean("learning_mode", true)
        playTTS = prefs.getBoolean("tts_mode", true)
        return this
    }
    fun clear() {
        prefs.edit().remove("vertical_text").apply()
        prefs.edit().remove("learning_mode").apply()
        prefs.edit().remove("tts_mode").apply()
    }
}