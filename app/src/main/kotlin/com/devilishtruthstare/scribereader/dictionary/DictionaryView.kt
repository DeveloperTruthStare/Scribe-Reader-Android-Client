package com.devilishtruthstare.scribereader.dictionary

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token

class DictionaryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    startingSearch: String? = null
) : FrameLayout(context, attrs) {
    private val entryContainer: LinearLayout
    private val noEntriesFoundMessage: TextView
    private val searchBar: EditText
    private val tokenContainer: LinearLayout
    private val t = tokenizer.Tokenizer.newTokenizer()
    private val jmDict = JMDict.getInstance(context)
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
        val tokens = DictionaryUtils.jsonToTokens(tokensJson)

        searchResults.clear()
        tokenContainer.removeAllViews()

        for ((index, token) in tokens.withIndex()) {
            val entries = jmDict.getEntries(Token.getSearchTerm(token))
            searchResults.add(Pair(token, entries))

            val tokenButton = TokenButton(context, null, token) {
                displayResults(index)
            }

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

    private fun displayResults(index: Int) {
        if (index >= searchResults.size) {
            return
        }

        val token = searchResults[index].first
        val entries = searchResults[index].second

        val searchTerm = Token.getSearchTerm(token)

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
                val exampleSentences = JMDict.getInstance(context).getExampleSentences(entry!!.entSeq)
                for (sentence in exampleSentences) {
                    Log.d("Example Sentence for ${entry.kanji}", sentence)
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
                sense.pos[i] = DictionaryUtils.convertPOS(sense.pos[i])
            }
            posText.text = sense.pos.toString()

            findViewById<TextView>(R.id.glossText).text = resources.getString(R.string.gloss_text, index+1, sense.gloss.toString())
        }
    }
}