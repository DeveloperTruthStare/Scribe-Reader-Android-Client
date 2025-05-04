package com.devilishtruthstare.scribereader.dictionary

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token

class TokenButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    token: Token? = null,
    onClickListener: (() -> Unit) = {}
) : FrameLayout(context, attrs) {
    private val surfaceDisplay: TextView
    private val furiganaDisplay: TextView
    init {
        LayoutInflater.from(context).inflate(R.layout.component_token_button, this, true)

        surfaceDisplay = findViewById(R.id.display)
        furiganaDisplay = findViewById(R.id.furigana)

        surfaceDisplay.text = token!!.surface
        val furigana = Token.getFurigana(token)
        if (furigana != token.surface) {
            furiganaDisplay.text = furigana
        } else {
            furiganaDisplay.text = ""
        }
        setOnClickListener {
            onClickListener()
        }
    }
}