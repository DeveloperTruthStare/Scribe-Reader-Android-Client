package com.devilishtruthstare.scribereader.ui.reader

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.book.utils.BookParser
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.ui.MainActivity
import com.devilishtruthstare.scribereader.ui.reader.content.BookContentAdapter
import kotlinx.coroutines.launch
import java.util.Locale


class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        internal enum class State {
            LOADING_BOOK, PREP_NEXT_PARAGRAPH, FINISH_PARAGRAPH, LEARNING_MODULE, NEXT_PARAGRAPH, DISPLAYING_PARAGRAPH
        }
    }
    private var state: State = State.LOADING_BOOK
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var titleText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var continueButton: AppCompatButton

    private lateinit var adapter: BookContentAdapter

    private lateinit var tts: TextToSpeech
    private var ttsReady: Boolean = false
    private lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reader)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tts = TextToSpeech(this, this)

        // Get information from the intent
        val bookId = intent.getIntExtra(resources.getString(R.string.EXTRA_BOOK_ID), -1)
        val recordKeeper = RecordKeeper.getInstance(this)
        book = recordKeeper.getBookById(bookId)!!


        // Get references to UI Elements
        linearLayoutManager = LinearLayoutManager(this)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = linearLayoutManager

        adapter = BookContentAdapter(book, this)
        recyclerView.adapter = adapter

        titleText = findViewById(R.id.reader_title)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        continueButton = findViewById(R.id.continue_button)
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            nextState()
        }

        titleText.text = book.title

        setupStates()
        lifecycleScope.launch {
            book = BookParser.parseBook(this@Reader, book).await()
            setupReader()
        }
    }
    private fun playSound(text: String) {
        if (ttsReady)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    private fun setupReader() {
        book.openBook(this)
        updateProgressBar()
        for(i in 0..book.currentSection) {
            val section = book.getParagraph(i)
            if (!section.isImage) {
                section.onPlaySoundClick = {
                    playSound(section.content)
                }
            }
            adapter.notifyItemInserted(i)
        }
        linearLayoutManager.scrollToPosition(book.currentSection+1)
        continueButton.isEnabled = true
        nextState()
    }


    private var stateMap: MutableMap<State, (() -> Pair<State, Boolean>)> = mutableMapOf()
    private fun setupStates() {
        stateMap[State.LOADING_BOOK] = {
            Pair(State.DISPLAYING_PARAGRAPH, false)
        }
        stateMap[State.PREP_NEXT_PARAGRAPH] = {
            val hasNewWords = book.prepareNextSection(this)
            if (hasNewWords) {
                adapter.showingLearningModule = true
                adapter.notifyItemChanged(book.currentSection)
                linearLayoutManager.scrollToPosition(book.currentSection+2)
                Pair(State.LEARNING_MODULE, false)
            } else {
                Pair(State.NEXT_PARAGRAPH, true)
            }
        }
        stateMap[State.LEARNING_MODULE] = {
            // Commit the Learning data
            adapter.showingLearningModule = false
            Pair(State.NEXT_PARAGRAPH, true)
        }
        stateMap[State.NEXT_PARAGRAPH] = {
            nextParagraph()
            Pair(State.DISPLAYING_PARAGRAPH, false)
        }
        stateMap[State.DISPLAYING_PARAGRAPH] = {
            Pair(State.FINISH_PARAGRAPH, true)
        }
        stateMap[State.FINISH_PARAGRAPH] = {
            finishParagraph()
            Pair(State.PREP_NEXT_PARAGRAPH, true)
        }
    }
    private fun nextState() {
        var res = stateMap[state]!!.invoke()
        state = res.first
        while(res.second) {
            res = stateMap[state]!!.invoke()
            state = res.first
        }
    }

    private fun finishParagraph() {
        for (token in book.getCurrentSection().tokens) {
            if (token.features.isEmpty() || token.features[0] in Token.IGNORED_MARKERS) continue
            val entries = JMDict.getInstance(this).getEntries(Token.getSearchTerm(token))
            JMDict.getInstance(this).updateEntries(entries) { entry ->
                entry.level+1
            }
        }
        book.getCurrentSection().isActive = false
        adapter.notifyItemChanged(book.currentSection+1)
    }

    private fun nextParagraph () {
        val result = book.next()
        when (result) {
            Book.END_OF_BOOK -> {
                RecordKeeper.getInstance(this).finishBook(book.bookId)
                startActivity(Intent(this, MainActivity::class.java))
                return
            }
            Book.NEXT_CHAPTER -> {
                adapter.notifyItemRangeRemoved(2, book.chapters[book.currentChapter-1].content.size+2)
                adapter.notifyItemChanged(1)
            }
            Book.NEXT_PARAGRAPH -> {
                adapter.notifyItemChanged(book.currentSection)
            }
        }
        updateProgressBar()
        val nextSection = book.getCurrentSection()

        if (!nextSection.isImage) {
            nextSection.onPlaySoundClick = {
                if (ttsReady)
                    tts.speak(nextSection.content, TextToSpeech.QUEUE_FLUSH, null, null)
            }
            nextSection.onPlaySoundClick()
        }

        linearLayoutManager.scrollToPosition(book.currentSection+1)

        RecordKeeper.getInstance(this).setProgress(
            book.bookId,
            book.currentChapter,
            book.currentSection
        )
    }

    private fun updateProgressBar() {
        progressBar.progress = book.currentSection
        val text = "Section ${book.currentChapter+1}: ${book.currentSection+1}/${book.chapters[book.currentChapter].content.size}"
        progressText.text = text
    }

    // TTS Engine Management
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set the language for the TTS engine
            val result = tts.setLanguage(Locale.JAPANESE) // For Japanese, use Locale.JAPANESE

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language not supported")
                startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA))
            } else {
                ttsReady = true
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
