package com.devilishtruthstare.scribereader.jmdict.ui

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
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.devilishtruthstare.scribereader.jmdict.Dictionary
import com.devilishtruthstare.scribereader.settings.ui.SettingsPopup
import com.devilishtruthstare.scribereader.utils.TokenizerUtils
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetDialog

class DictionaryView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    private var params: DictionaryViewParams = DictionaryViewParams()
) : FrameLayout(context, attrs) {
    companion object {
        data class DictionaryViewParams (
            var startingSearch: String = "",
            var isFullscreen: Boolean = true,
            var tokenizeText: Boolean = true
        )
    }
    private val entryContainer: LinearLayout
    private val noEntriesFoundMessage: TextView
    private val searchBar: EditText
    private val tokenContainer: FlexboxLayout
    private val dictionary = Dictionary.getInstance(context)
    private var searchResults = mutableListOf<Pair<Token, List<Entry>>>()
    private val header: LinearLayout
    private val expandButton: AppCompatButton
    private val settingsButton: AppCompatButton

    init {
        LayoutInflater.from(context).inflate(R.layout.dictionary_view, this, true)

        entryContainer = findViewById(R.id.entryContainer)
        noEntriesFoundMessage = findViewById(R.id.noEntryFound)
        tokenContainer = findViewById(R.id.sentenceTokenizationContainer)
        searchBar = findViewById(R.id.searchText)
        searchBar.setText(params.startingSearch)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (params.tokenizeText) {
                    displayTokenization(s.toString())
                } else {

                }
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

        header = findViewById(R.id.header)

        expandButton = findViewById(R.id.expand_button)
        expandButton.setOnClickListener {
            setFullscreen(true)
        }

        settingsButton = findViewById(R.id.settings_button)
        initializeSettings()

        if (params.startingSearch.isNotBlank()) {
            displayTokenization(params.startingSearch)
        }

        setFullscreen(params.isFullscreen)
    }

    private fun setFullscreen(isFullscreen: Boolean) {
        searchBar.visibility = if (isFullscreen) VISIBLE else GONE
        header.visibility = if (isFullscreen) VISIBLE else GONE
        tokenContainer.visibility = if (isFullscreen) VISIBLE else GONE
        expandButton.visibility = if (isFullscreen) GONE else VISIBLE
        background = if (isFullscreen) resources.getDrawable(R.drawable.tab_view_background) else resources.getDrawable(R.drawable.rounded_popup)
        header.setBackgroundColor(if(isFullscreen) resources.getColor(R.color.tertiary_container) else resources.getColor(R.color.primary_container))
    }

    private fun initializeSettings() {
        settingsButton.visibility = if (params.isFullscreen) VISIBLE else GONE
        val settingsController = SettingsPopup(context).apply {
            addSetting("Tokenize Text", params.tokenizeText) { isEnabled ->
                params.tokenizeText = isEnabled
            }
        }
        val settingsDialog = BottomSheetDialog(context)
        settingsDialog.setContentView(settingsController)
        settingsButton.setOnClickListener {
            settingsDialog.show()
        }
    }

    private fun displayTokenization(input: String) {
        val tokens = TokenizerUtils.getInstance().tokenize(input)

        searchResults.clear()
        tokenContainer.removeAllViews()

        for ((index, token) in tokens.withIndex()) {
            val entries = dictionary.search(Token.getSearchTerm(token))
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

        val searchTerm = Token.getSearchTerm(token)

        entryContainer.removeAllViews()
        for (entry in entries) {
            entryContainer.addView(EntryView(context, entry = entry, searchTerm = searchTerm))
        }
    }
}