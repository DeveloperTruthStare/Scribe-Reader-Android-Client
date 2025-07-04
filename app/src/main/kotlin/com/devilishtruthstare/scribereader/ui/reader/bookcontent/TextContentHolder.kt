package com.devilishtruthstare.scribereader.ui.reader.bookcontent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.jmdict.Dictionary
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.devilishtruthstare.scribereader.jmdict.ui.DictionaryView
import com.google.android.flexbox.FlexboxLayout
import kotlin.math.max
import kotlin.math.min

class TextContentHolder(
    private val itemView: View,
    private val onTTSClick: (text: String) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val textContainer: FlexboxLayout = itemView.findViewById(R.id.container)
    private val ttsButton: ImageView = itemView.findViewById(R.id.tts_button)
    private val textViews: MutableList<JPNTextView> = mutableListOf()
    private val dictionary: Dictionary = Dictionary.getInstance(itemView.context)

    fun bind(section: Content) {
        textContainer.removeAllViews()
        textViews.clear()

        for(token in section.tokens) {
            val textView =
                JPNTextView(
                    itemView.context, params = JPNTextView.JPNTextViewParams(
                    token = token,
                    onClick = { textView ->
                        if (section.isActive) {
                            onTokenClick(textView, token)
                        }
                    }, {}
                ))

            if (!section.isActive) {
                textView.setUnderlineVisible(false)
                textView.setFuriganaVisible(false)
            }


            textViews.add(textView)
            textContainer.addView(textView)
        }
        ttsButton.visibility = if (section.isActive) View.VISIBLE else View.GONE
        ttsButton.setOnClickListener {
            onTTSClick(section.content)
        }
    }
    private fun onTokenClick(textView: JPNTextView, token: Token) {
        showDefinitionPopup(itemView, token)
        val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", Token.getSearchTerm(token))
        clipboard.setPrimaryClip(clip)

        lowerLevel(textView.getRelaventEntries())
        refreshTextViews()
    }

    private fun refreshTextViews() {
        for (view in textViews) {
            view.setupView()
        }
    }
    private fun showDefinitionPopup(anchorView: View, token: Token) {
        val context = anchorView.context
        val definitionView = DictionaryView(context, params = DictionaryView.Companion.DictionaryViewParams(
            Token.getSearchTerm(token),
            isFullscreen = false))

        // Convert dp to pixels
        val displayMetrics = context.resources.displayMetrics
        val marginWidth = (20 * displayMetrics.density).toInt()

        // Get screen dimensions
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calculate popup dimensions
        val popupWidth = screenWidth - (2 * marginWidth)  // Full width minus margins
        val popupHeight = screenHeight * 2 / 3

        // Create PopupWindow with calculated dimensions
        val definitionWindow = PopupWindow(definitionView, popupWidth, popupHeight, true)

        definitionWindow.isOutsideTouchable = true

        definitionWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }

    private fun lowerLevel(entries: List<Entry>) {
        dictionary.setEntryLevels(entries) { entry ->
            max(1, min(entry.level - 2, 3))
        }
    }

}
