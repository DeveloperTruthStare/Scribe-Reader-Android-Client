package com.devilishtruthstare.scribereader.settings.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.appcompat.widget.SwitchCompat
import com.devilishtruthstare.scribereader.R

class SettingsPopup(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    companion object {
        val LEARNING_MODE = Setting.LEARNING_MODE
        val VERTICAL_MODE = Setting.VERTICAL_MODE
        val TTS_MODE = Setting.TTS_MODE

        enum class Setting {
            LEARNING_MODE, VERTICAL_MODE, TTS_MODE
        }
    }
    private val settingMap: MutableMap<Setting, SwitchCompat> = mutableMapOf()
    private val verticalSwitch: SwitchCompat
    private val learningSwitch: SwitchCompat
    private val ttsSwitch: SwitchCompat

    init {
        LayoutInflater.from(context).inflate(R.layout.reader_settings_popup, this, true)

        verticalSwitch = findViewById(R.id.reader_vertical_setting_switch)
        learningSwitch = findViewById(R.id.reader_learning_setting_switch)
        ttsSwitch = findViewById(R.id.reader_tts_setting_switch)

        settingMap[Setting.VERTICAL_MODE] = verticalSwitch
        settingMap[Setting.LEARNING_MODE] = learningSwitch
        settingMap[Setting.TTS_MODE] = ttsSwitch
    }

    fun setOnToggleChanged(mode: Setting, callback: (Boolean) -> Unit, status: Boolean = false) {
        if (!settingMap.containsKey(mode)) {
            return
        }
        settingMap[mode]?.setOnCheckedChangeListener { _, isChecked ->
            callback(isChecked)
        }
        settingMap[mode]?.isChecked = status
    }
}