package com.devilishtruthstare.scribereader.ui.reader.bookcontent

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.devilishtruthstare.scribereader.jmdict.Dictionary

class JPNTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val params: JPNTextViewParams
) : FrameLayout(context, attrs) {
    data class JPNTextViewParams(
        val token: Token,
        val onClick: (textView: JPNTextView) -> Unit,
        val onLongClick: (textView: JPNTextView) -> Unit,
    )

    private val furiganaView: TextView
    private val underline: View
    private val view: View =
        LayoutInflater.from(context).inflate(R.layout.base_text_view, this, true)

    private var furiganaText: String = ""

    private var relatedEntries: List<Entry> = emptyList()
    private var parseToken: Boolean = true

    private var highestLevel = -1
    fun getHighestLevel(): Int {
        return highestLevel
    }

    init {
        view.findViewById<TextView>(R.id.token_text).text = params.token.surface
        furiganaView = view.findViewById(R.id.furigana_text)
        underline = view.findViewById(R.id.underline)

        initialize()
    }

    private fun initialize() {
        if (params.token.features.isEmpty() || params.token.features[0] in Token.IGNORED_MARKERS) {
            parseToken = false
            setUnderlineVisible(false)
            setFuriganaVisible(false)
            return
        }

        setupView()

        view.setOnClickListener {
            params.onClick(this)
        }
        view.setOnLongClickListener {
            params.onLongClick(this)
            true
        }
    }

    fun setUnderlineVisible(isVisible: Boolean) {
        underline.visibility = if (isVisible && parseToken) VISIBLE else GONE
    }

    private fun setUnderlineColor(@ColorInt color: Int) {
        underline.setBackgroundColor(color)
    }

    fun setFuriganaVisible(isVisible: Boolean) {
        furiganaView.text = if (isVisible && parseToken) furiganaText else ""
    }

    private fun loadEntries() {
        relatedEntries = Dictionary.getInstance(context).search(Token.getSearchTerm(params.token))
        highestLevel = -1
        for (entry in relatedEntries) {
            if (entry.level > highestLevel) {
                highestLevel = entry.level
            }
        }
    }
    private fun getFuriganaText(entries: List<Entry>): String {
        var text = Token.getFurigana(params.token)
        if (text == "" && Token.containsKanji(params.token)) {
            for (entry in entries) {
                if (entry.kana.isNotEmpty()) {
                    text = entry.kana[0]
                    break
                }
            }
        }
        return text
    }
    fun getRelaventEntries(): List<Entry> {
        return relatedEntries
    }

    fun setupView() {
        if (!parseToken) return

        loadEntries()
        furiganaText = getFuriganaText(relatedEntries)

        setUnderlineVisible(false)
        setFuriganaVisible(false)
        if (highestLevel < Dictionary.LEARNED_LEVEL) {
            setUnderlineVisible(true)
            var underlineColor = "#f59e0b"
            if (highestLevel == Dictionary.FIRST_VIEW_LEVEL) {
                underlineColor = "#00ff00"
            } else if (highestLevel <= Dictionary.MAX_LEARNING_LEVEL) {
                setFuriganaVisible(true)
            }

            if (highestLevel == -1) {
                // Word not found in dictionary. Could be tokenization error, or proper name actually not found
                underlineColor = "#ff0000"
            }
            setUnderlineColor(underlineColor.toColorInt())
        }
    }
}