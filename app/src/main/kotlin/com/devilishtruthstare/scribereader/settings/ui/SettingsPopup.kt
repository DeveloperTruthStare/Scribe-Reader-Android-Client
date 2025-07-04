package com.devilishtruthstare.scribereader.settings.ui

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat

class SettingsPopup(
    context: Context
) : FrameLayout(context) {
    private val contentLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(50, 50, 50, 100)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }
    private val settingMap: MutableSet<String> = mutableSetOf()

    init {
        addView(contentLayout)
    }

    fun addSetting(displayText: String, initialStatus: Boolean = false, callback: (Boolean) -> Unit) {
        if (settingMap.contains(displayText)) return

        contentLayout.addView(SwitchCompat(context).apply {
            text = displayText
            isChecked = initialStatus
            setOnCheckedChangeListener { _, isChecked ->
                callback(isChecked)
            }
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        })
        settingMap.add(displayText)
    }
}