package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token

class TokenReadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    token: Token
) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.popup_token_details, this, true)

        val readingText = findViewById<TextView>(R.id.reading)
        val dictionaryFormText = findViewById<TextView>(R.id.word)
        val partOfSpeechText = findViewById<TextView>(R.id.partOfSpeech)

        if (token.features.size >= 8) {
            readingText.text = token.features[7]
        } else {
            readingText.visibility = GONE
        }

        if (token.features.size >= 7) {
            dictionaryFormText.text = token.features[6]
        } else {
            dictionaryFormText.visibility = GONE
        }

        if (token.features.isNotEmpty()) {
            partOfSpeechText.text = token.features[0]
        } else {
            partOfSpeechText.visibility = GONE
        }
    }
}