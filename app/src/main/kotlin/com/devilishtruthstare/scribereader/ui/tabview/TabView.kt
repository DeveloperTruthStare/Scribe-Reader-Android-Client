package com.devilishtruthstare.scribereader.ui.tabview

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.databinding.TabViewBinding

class TabView @JvmOverloads constructor(
    context: Context,
) : FrameLayout(context) {
    private val binding = TabViewBinding.inflate(LayoutInflater.from(context), this)
    private var onNewTabClick: (() -> Unit)? = null
    fun setOnNewTabClick(callback: () -> Unit) {
        onNewTabClick = callback
    }

    init {
        binding.newTabButton.setOnClickListener {
            if (onNewTabClick != null) {
                onNewTabClick!!()
            }
        }
    }

    fun addTab(tabItem: TabItem) {
        val tab = TextView(context).apply {
            text = tabItem.getTitle()
            setPadding(24, 12, 24, 12)
            background = ContextCompat.getDrawable(context, R.drawable.tab_background)
            setTextColor(Color.WHITE)
            setOnClickListener {
                tabItem.onSelected()
            }

            layoutParams = LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 16
            }
        }

        binding.tabContainer.addView(tab, binding.tabContainer.size-1)
    }
}
