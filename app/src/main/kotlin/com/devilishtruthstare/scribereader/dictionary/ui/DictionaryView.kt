package com.devilishtruthstare.scribereader.dictionary.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.dictionary.DictionaryUtils
import com.devilishtruthstare.scribereader.dictionary.Entry
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.dictionary.Sense
import com.devilishtruthstare.scribereader.dictionary.ui.TokenButton
import com.google.android.flexbox.FlexboxLayout
import tokenizer.Tokenizer

class DictionaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    startingSearch: String? = null
) : FrameLayout(context, attrs) {
    private val entryContainer: LinearLayout
    private val noEntriesFoundMessage: TextView
    private val searchBar: EditText
    private val tokenContainer: FlexboxLayout
    private val t = Tokenizer.newTokenizer()
    private val jmDict = JMDict.Companion.getInstance(context)
    private var searchResults = mutableListOf<Pair<Token, List<Entry>>>()

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dictionary, this, true)

        entryContainer = findViewById(R.id.entryContainer)
        noEntriesFoundMessage = findViewById(R.id.noEntryFound)
        tokenContainer = findViewById(R.id.sentenceTokenizationContainer)
        searchBar = findViewById(R.id.searchText)
        searchBar.setText(startingSearch)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                displayTokenization(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        searchBar.setOnEditorActionListener { _, actionId, event ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchBar.windowToken, 0)

            actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
        }

        displayTokenization(startingSearch ?: "")
    }

    private fun displayTokenization(input: String) {
        val tokensJson = t.tokenize(input)
        val tokens = DictionaryUtils.Companion.jsonToTokens(tokensJson)

        searchResults.clear()
        tokenContainer.removeAllViews()

        for ((index, token) in tokens.withIndex()) {
            val entries = jmDict.getEntries(Token.Companion.getSearchTerm(token))
            searchResults.add(Pair(token, entries))

            val tokenButton = TokenButton(context, null, token) {
                displayResults(index)
                unselectAllTokens()
            }
            if (index == 0) tokenButton.setSelected()
            tokenContainer.addView(tokenButton)
        }

        if (searchResults.isNotEmpty()) {
            displayResults(0)
            noEntriesFoundMessage.visibility = GONE
        } else {
            entryContainer.removeAllViews()
            noEntriesFoundMessage.visibility = VISIBLE
        }
    }
    private fun unselectAllTokens() {
        for (tokenButton in tokenContainer.children) {
            (tokenButton as TokenButton).setUnselected()
        }
    }
    private fun displayResults(index: Int) {
        if (index >= searchResults.size) {
            return
        }

        val token = searchResults[index].first
        val entries = searchResults[index].second

        val searchTerm = Token.Companion.getSearchTerm(token)

        entryContainer.removeAllViews()
        for (entry in entries) {
            entryContainer.addView(EntryView(context, entry = entry, searchTerm = searchTerm))
        }
    }

    private class EntryView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, entry: Entry? = null, searchTerm: String = "") : FrameLayout(context, attrs) {
        private val exampleSentenceButton: TextView
        private val kanjiText: TextView
        private val kanaText: TextView
        private val senseContainer: LinearLayout
        init {
            LayoutInflater.from(context).inflate(R.layout.view_entry, this, true)
            exampleSentenceButton = findViewById(R.id.viewExampleSentences)
            exampleSentenceButton.setOnClickListener {
                val exampleSentences = JMDict.Companion.getInstance(context).getExampleSentences(entry!!.entSeq)
                for (sentence in exampleSentences) {
                    val view = LayoutInflater.from(context).inflate(R.layout.item_text, this, true)
                    var part1 = ""
                    var part2 = ""
                    var searchToken: Token? = null
                    var found = false
                    for (token in sentence) {
                        val searchTerm = Token.Companion.getSearchTerm(token)
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
            }

            kanjiText = findViewById(R.id.kanjiPrimary)
            kanaText = findViewById(R.id.kanaPrimary)
            kanjiText.text = if (entry!!.kanji.isNotEmpty()) {
                entry.kanji.toString()
            } else {
                searchTerm
            }

            kanaText.text = if (entry.kana.isNotEmpty()) {
                entry.kana.toString()
            } else {
                searchTerm
            }

            senseContainer = findViewById(R.id.senses)
            entry.senses.forEachIndexed { index, sense ->
                senseContainer.addView(SenseView(context, index = index, sense = sense))
            }
        }
    }
    private class SenseView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, index: Int = 1, sense: Sense? = null) : FrameLayout(context, attrs) {
        private val posText: TextView
        init {
            LayoutInflater.from(context).inflate(R.layout.component_sense, this, true)
            posText = findViewById(R.id.posText)

            for (i in sense!!.pos.indices) {
                sense.pos[i] = DictionaryUtils.Companion.convertPOS(sense.pos[i])
            }
            posText.text = sense.pos.toString()

            findViewById<TextView>(R.id.glossText).text = resources.getString(R.string.gloss_text, index+1, sense.gloss.toString())
        }
    }
}