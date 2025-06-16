package com.devilishtruthstare.scribereader.ui.reader.bookcontent

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.dictionary.Entry
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.dictionary.ui.DictionaryView
import kotlin.math.max
import kotlin.math.min

class TokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val token: Token,
    private val onUpdated: (() -> Unit),
    isActive: Boolean
) : FrameLayout(context, attrs) {
    companion object {
        internal const val MAX_LEARNING_LEVEL = 2
        internal const val FIRST_VIEW_LEVEL = 0
        internal const val LEARNED_LEVEL = 5
    }
    private val furiganaView: TextView
    private val underline: View
    private val button: View =
        LayoutInflater.from(context).inflate(R.layout.component_token, this, true)

    private var furiganaText: String = ""

    init {
        // Set Surface of token
        button.findViewById<TextView>(R.id.token_text).text = token.surface
        furiganaView = button.findViewById(R.id.furigana_text)
        furiganaView.visibility = GONE

        underline = button.findViewById(R.id.underline)
        underline.visibility = GONE

        if (isActive) {
            furiganaView.visibility = VISIBLE
            setupView()
        }
    }

    fun setupView() {
        underline.visibility = GONE
        furiganaView.text = ""

        if (token.features.isEmpty() || token.features[0] in Token.IGNORED_MARKERS) {
            return
        }

        val entries = JMDict.getInstance(context).getEntries(Token.getSearchTerm(token))
        furiganaText = getFuriganaText(entries)

        var highestLevel = -1
        for(entry in entries) {
            if (entry.level > highestLevel) {
                highestLevel = entry.level
            }
        }

        if (highestLevel < LEARNED_LEVEL) {
            underline.visibility = VISIBLE
            var underlineColor = "#f59e0b"
            if (highestLevel == FIRST_VIEW_LEVEL) {
                underlineColor = "#00ff00"
            }
            if (highestLevel == -1) {
                underlineColor = "#ff0000"
            }
            if (highestLevel <= MAX_LEARNING_LEVEL) {
                furiganaView.text = furiganaText
            }
            underline.setBackgroundColor(underlineColor.toColorInt())
        }

        button.setOnClickListener { view ->
            showDefinitionPopup(view, token)
            lowerLevel(entries)
        }
        button.setOnLongClickListener {
            if (highestLevel == 0) {
                markAsKnown(entries)
                true
            } else {
                // TODO: Show options for this token
                false
            }
        }
    }

    private fun getFuriganaText(entries: List<Entry>): String {
        var text = Token.getFurigana(token)
        if (text == "" && Token.containsKanji(token)) {
            for (entry in entries) {
                if (entry.kana.size > 0) {
                    text = entry.kana[0]
                    break
                }
            }
        }
        return text
    }

    private fun markAsKnown(entries: List<Entry>) {
        for (entry in entries) {
            JMDict.getInstance(context).updateEntry(entry.entSeq, LEARNED_LEVEL)
        }
        onUpdated()
    }

    private fun lowerLevel(entries: List<Entry>) {
        for (entry in entries) {
            JMDict.getInstance(context).updateEntry(entry.entSeq, max(1, min(entry.level-2, 3)))
        }
        furiganaView.text = furiganaText
        underline.visibility = VISIBLE
        onUpdated()
    }

    private fun showDefinitionPopup(anchorView: View, token: Token) {
        val context = anchorView.context
        val definitionView = DictionaryView(context, startingSearch = Token.getSearchTerm(token))

        // Convert dp to pixels
        val marginWidth = (20 * context.resources.displayMetrics.density).toInt()

        // Get screen dimensions
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calculate popup dimensions
        val popupWidth = screenWidth - (2 * marginWidth)  // Full width minus margins
        val popupHeight = screenHeight * 3 / 4

        // Create PopupWindow with calculated dimensions
        val definitionWindow = PopupWindow(definitionView, popupWidth, popupHeight, true)

        definitionWindow.isOutsideTouchable = true

        definitionWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }
}