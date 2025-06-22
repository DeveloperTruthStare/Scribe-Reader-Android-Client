package com.devilishtruthstare.scribereader.jmdict.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.jmdict.data.Entry


internal class EntryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    entry: Entry? = null,
    searchTerm: String = ""
) : FrameLayout(context, attrs) {
    //private val exampleSentenceButton: TextView
    private val kanjiText: TextView
    private val kanaText: TextView
    private val senseContainer: LinearLayout
    private val levelText: TextView
    init {
        LayoutInflater.from(context).inflate(R.layout.dictionary_entry_view, this, true)
        /*exampleSentenceButton = findViewById(R.id.viewExampleSentences)
        exampleSentenceButton.setOnClickListener {
            val exampleSentences = LibraryDB.getInstance(context).getExampleSentences(entry!!.entSeq)
            for (sentence in exampleSentences) {
                val view = LayoutInflater.from(context).inflate(R.layout.item_text, this, true)
                var part1 = ""
                var part2 = ""
                var searchToken: Token? = null
                var found = false
                for (token in sentence) {
                    val searchTerm = Token.getSearchTerm(token)
                    if (searchTerm in entry.kanji || searchTerm in entry.kana) {
                        found = true
                        searchToken = token
                    } else {
                        if (found) {
                            part1 += token.surface
                        } else {
                            part2 += token.surface
                        }
                    }
                }

            }
        }*/

        kanjiText = findViewById(R.id.kanjiPrimary)
        kanjiText.text = if (entry!!.kanji.isNotEmpty()) {
            entry.kanji.toString()
        } else {
            "N/A"
        }

        kanaText = findViewById(R.id.kanaPrimary)
        kanaText.text = if (entry.kana.isNotEmpty()) {
            entry.kana.toString()
        } else {
            "N/A"
        }

        levelText = findViewById(R.id.entry_level)
        levelText.text = entry.level.toString()

        senseContainer = findViewById(R.id.senses)
        entry.senses.forEachIndexed { index, sense ->
            senseContainer.addView(SenseView(context, index = index, sense = sense))
        }
    }
}