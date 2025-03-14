package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.jmdict.Entry
import com.devilishtruthstare.scribereader.jmdict.JMDict
import com.devilishtruthstare.scribereader.jmdict.Sense
import com.google.android.flexbox.FlexboxLayout
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.database.FlashCard
import com.devilishtruthstare.scribereader.database.UserStats


class TextContentHolder(
    itemView: View,
    private val context: Context,
) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val PUNCTUATION_MARKER = "記号"
        private const val PARTICLE_MARKER = "助詞"
        private const val CONJUGATION_MARKER = "助動詞"
        private const val EMPTY_MARKER = "*"
    }
    private val textContainer: FlexboxLayout = itemView.findViewById(R.id.container)
    private val ttsButton: ImageView = itemView.findViewById(R.id.tts_button)

    private lateinit var definitionView: View
    private lateinit var entryContainer: LinearLayout
    private lateinit var noEntriesFoundMessage: TextView

    private lateinit var flashCard: FlashCard
    fun bind(section: Content) {
        textContainer.removeAllViews()
        for(token in section.tokens) {
            val button = LayoutInflater.from(itemView.context).inflate(R.layout.token_view, null)
            val textView = button.findViewById<TextView>(R.id.token_text)
            val underline = button.findViewById<View>(R.id.underline)

            textView.text = token.surface
            textView.textSize = 24f

            if (token.features.isEmpty() ||
                token.features[0] == PUNCTUATION_MARKER ||
                token.features[0] == PARTICLE_MARKER ||
                token.features[0] == CONJUGATION_MARKER) {
                underline.visibility = View.GONE
            } else {
                button.setOnClickListener { view ->
                    showReadingPopup(view, token)
                }

                button.setOnLongClickListener { view ->
                    showDefinitionPopup(view, token)
                    true
                }
            }
            textContainer.addView(button)
        }
        flashCard = FlashCard(context)
        ttsButton.setOnClickListener {
            // Set tts Player
        }
    }

    private fun showDefinitionPopup(anchorView: View, token: Token) {
        // Create UI
        definitionView = LayoutInflater.from(anchorView.context).inflate(R.layout.definition_popup, null)
        entryContainer = definitionView.findViewById<LinearLayout>(R.id.entryContainer)
        noEntriesFoundMessage = definitionView.findViewById<TextView>(R.id.noEntryFound)
        val searchBar = definitionView.findViewById<EditText>(R.id.searchText)

        // Convert dp to pixels
        val marginWidth = (25 * context.resources.displayMetrics.density).toInt()

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


        // Update UI
        val searchTerm = getSearchWord(token)

        searchBar.setText(searchTerm)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                displayResults(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        displayResults(searchTerm)
    }

    private fun displayResults(input: String) {
        entryContainer.removeAllViews()
        val entries = getEntries(input)

        for(entry in entries) {
            val entryView = getEntryView(entry, definitionView, input)
            entryContainer.addView(entryView)
        }
        noEntriesFoundMessage.visibility = if (entries.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun getEntryView(entry: Entry, popupView: View, searchTerm: String): View {
        val entryView = LayoutInflater.from(popupView.context).inflate(R.layout.entry_fragment, null)

        val kanjiText = entryView.findViewById<TextView>(R.id.kanjiPrimary)
        kanjiText.text = if (entry.kanji.isNotEmpty()) {
            entry.kanji[0]
        } else {
            searchTerm
        }

        val kanaText = entryView.findViewById<TextView>(R.id.kanaPrimary)
        kanaText.text = if (entry.kana.isNotEmpty()) {
            entry.kana[0]
        } else {
            searchTerm
        }

        val addButton = entryView.findViewById<ImageButton>(R.id.add_button)
        if (!flashCard.hasEntry(entry.entSeq)) {
            addButton.setOnClickListener {
                addButton.setImageResource(android.R.drawable.ic_input_get)
                flashCard.addEntry(entry.entSeq, "LEARNING")
            }
        } else {
            val status = flashCard.getEntry(entry.entSeq)
            if (status == "LEARNING") {
                addButton.setImageResource(android.R.drawable.ic_input_get)
            } else if (status == "LEARNT") {
                addButton.setImageResource(android.R.drawable.ic_delete)
            }
        }

        val senseContainer = entryView.findViewById<LinearLayout>(R.id.senses)
        entry.senses.forEachIndexed { index, sense ->
            val senseView = getSenseView(index, sense, entryView)
            senseContainer.addView(senseView)
        }

        return entryView
    }
    private fun getSenseView(index: Int, sense: Sense, entryView: View): View {
        val senseView = LayoutInflater.from(entryView.context).inflate(R.layout.sense_fragment, null)
        val posText = senseView.findViewById<TextView>(R.id.posText)
        posText.text = sense.pos.toString()

        val glossText = senseView.findViewById<TextView>(R.id.glossText)
        val text = "${index+1}. ${sense.gloss}"
        glossText.text = text

        return senseView
    }

    private fun getSearchWord(token: Token): String {
        return if (token.features.size >= 7 && token.features[6] != EMPTY_MARKER) {
            token.features[6]
        } else {
            token.surface
        }
    }

    private fun getEntries(searchingWord: String): List<Entry> {
        return JMDict(context).getEntries(searchingWord)
    }

    private fun showReadingPopup(anchorView: View, token: Token) {
        // Inflate the popup layout
        val popupView = LayoutInflater.from(anchorView.context).inflate(R.layout.token_details_popup, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set rounded corners for the popup window background
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(anchorView.context,
                R.drawable.rounded_popup
            ))
        popupWindow.isOutsideTouchable = true

        val readingText = popupView.findViewById<TextView>(R.id.reading)
        val baseWord = popupView.findViewById<TextView>(R.id.word)
        val partOfSpeech = popupView.findViewById<TextView>(R.id.partOfSpeech)

        if (token.features.size >= 8) {
            readingText.text = token.features[7]
        } else {
            readingText.visibility = View.GONE
        }

        if (token.features.size >= 7) {
            baseWord.text = token.features[6]
        } else {
            baseWord.visibility = View.GONE
        }

        if (token.features.isNotEmpty()) {
            partOfSpeech.text = token.features[0]
        } else {
            partOfSpeech.visibility = View.GONE
        }

        // Measure the popup width to center it properly
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        // Get the location of the button (anchorView)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorCenterX = location[0] + anchorView.width / 2

        // Calculate x position to center popup on the button
        val xOffset = anchorCenterX - popupWidth / 2

        val anchorCenterY = location[1] + anchorView.width / 2
        val yOffset = anchorCenterY + anchorView.measuredHeight / 2

        // Show the popup window offset from the button
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
}
