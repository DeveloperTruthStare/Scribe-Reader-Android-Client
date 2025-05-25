package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.dictionary.ui.DictionaryView


class TokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    token: Token,
    onLongClick: ((token: Token) -> Unit)
) : FrameLayout(context, attrs) {
    companion object {
        const val STATE_NORMAL = "NORMAL"
        const val STATE_FURIGANA = "FURIGANA"
    }

    fun setState(newState: String) {
        when (newState) {
            STATE_FURIGANA -> {
                furiganaView.visibility = VISIBLE
                underline.visibility = if (shouldShowUnderline) VISIBLE else GONE
            }
            else -> {
                furiganaView.visibility = GONE
                underline.visibility = GONE
            }
        }
    }
    private val surfaceView: TextView
    private val furiganaView: TextView
    private val underline: View

    private var shouldShowUnderline = true
    init {
        val button = LayoutInflater.from(context).inflate(R.layout.component_token, this, true)
        button.setOnLongClickListener { view ->
            onLongClick(token)
            true
        }

        surfaceView = button.findViewById(R.id.token_text)
        furiganaView = button.findViewById(R.id.furigana_text)
        underline = button.findViewById(R.id.underline)

        surfaceView.text = token.surface
        Log.d("Token: ${token.surface}", Token.getFurigana(token))
        furiganaView.text = Token.getFurigana(token)

        if (token.features.isEmpty() ||
            token.features[0] in Token.IGNORED_MARKERS) {
            shouldShowUnderline = false
        } else {
            val underlineColor = when (token.features[0]) {
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

            underline.setBackgroundColor(underlineColor.toColorInt())
        }
        setState(STATE_NORMAL)
    }
}