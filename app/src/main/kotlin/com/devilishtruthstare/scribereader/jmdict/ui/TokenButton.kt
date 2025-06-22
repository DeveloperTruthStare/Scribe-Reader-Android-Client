package com.devilishtruthstare.scribereader.jmdict.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
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
    private val underline: View
    private var defaultColor = "#6b7280"
    init {
        LayoutInflater.from(context).inflate(R.layout.component_token_button, this, true)

        surfaceDisplay = findViewById(R.id.display)
        furiganaDisplay = findViewById(R.id.furigana)
        underline = findViewById(R.id.underline)

        surfaceDisplay.text = token!!.surface
        val furigana = Token.getFurigana(token)
        if (furigana != token.surface) {
            furiganaDisplay.text = furigana
        } else {
            furiganaDisplay.text = ""
        }
        defaultColor = when (token.features[0]) {
            Token.NOUN_MARKER -> "#d97706"           // amber for nouns
            Token.PRE_NOUN_ADJECTIVAL -> "#f59e0b"   // lighter amber
            Token.CONJUNCTION_MARKER -> "#10b981"    // teal for conjunctions
            Token.VERB_MARKER -> "#3b82f6"           // blue for verbs
            Token.CONJUGATION_MARKER -> "#60a5fa"    // lighter blue
            Token.PREFIX_MARKER -> "#8b5cf6"         // violet for prefixes
            Token.INTERJECTION_MARKER -> "#ec4899"   // pink for interjections
            Token.I_ADJECTIVE_MARKER -> "#f43f5e"    // red for i-adjectives
            Token.ADVERB_MARKER -> "#14b8a6"         // cyan for adverbs
            else -> "#6b7280"                  // gray fallback
        }

        setColor(defaultColor)

        setOnClickListener {
            onClickListener()
            setSelected()
        }
    }

    fun setSelected() {
        setColor("#ffffff")
    }

    fun setUnselected() {
        setColor(defaultColor)
    }

    private fun setColor(color: String) {
        underline.setBackgroundColor(color.toColorInt())
        surfaceDisplay.setTextColor(color.toColorInt())
        furiganaDisplay.setTextColor(color.toColorInt())
    }
}