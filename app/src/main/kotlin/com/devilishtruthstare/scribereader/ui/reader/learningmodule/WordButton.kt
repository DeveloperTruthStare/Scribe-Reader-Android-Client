package com.devilishtruthstare.scribereader.ui.reader.learningmodule

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.R

class WordButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    displayText: String,
    isKnown: Boolean
    ) : FrameLayout(context, attrs) {

    private val button: View =
        LayoutInflater.from(context).inflate(R.layout.word_button, this, true)
    private val textView: TextView = button.findViewById(R.id.display_text)
    init {
        textView.text = displayText
        textView.background = if(isKnown) resources.getDrawable(R.drawable.known_background) else resources.getDrawable(R.drawable.bottom_border_rounded)
    }
    fun setKnown() {
        textView.background = resources.getDrawable(R.drawable.known_background)
        textView.setTextColor("#BEBEBE".toColorInt())
    }
}