package com.devilishtruthstare.scribereader.reader

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.dictionary.DictionaryUtils
import com.devilishtruthstare.scribereader.reader.content.BookContentAdapter
import kotlinx.coroutines.launch
import java.util.Locale


class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        const val EXTRA_BOOK_ID = "BOOK_ID"
        private const val NEXT_TEXT = "次"
        private const val NEXT_CHAPTER_TEXT = "次の第"
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var continueButton: Button

    private lateinit var adapter: BookContentAdapter
    private lateinit var contentList: MutableList<Content>


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
        val bookId = intent.getIntExtra(EXTRA_BOOK_ID, -1)

        contentList = mutableListOf()

        // Get references to UI Elements
        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.scrollToPosition(contentList.size)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = linearLayoutManager

        adapter = BookContentAdapter(contentList, this)
        recyclerView.adapter = adapter

        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        continueButton = findViewById(R.id.continue_button)
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            nextLineClick()
        }

        val recordKeeper = RecordKeeper.getInstance(this)
        val bookRecord = recordKeeper.getBookById(bookId)!!
        recordKeeper.addOpenHistory(bookId)
        lifecycleScope.launch {
            book = DictionaryUtils.parseBook(this@Reader, bookRecord.title).await()
            book.bookId = bookRecord.bookId
            book.currentSection = bookRecord.currentSection
            book.currentChapter = bookRecord.currentChapter
            progressBar.max = book.chapters.size
            updateProgressBar()
            continueButton.isEnabled = true
            nextLineClick()
        }
    }

    private fun updateProgressBar() {
        progressBar.progress = book.currentSection
        progressBar.max = book.chapters.size

        val text = "Chapter: ${book.currentChapter}: ${book.currentSection+1}/${book.chapters[book.currentChapter].content.size}"
        progressText.text = text
    }

    private fun nextLineClick () {
        if (book.currentSection == book.chapters[book.currentChapter].content.size-1) {
            if (book.currentChapter == book.chapters.size-1) {
                Log.e("TODO", "Book finished")
                // Intent go to library activity
                return
            }

            book.currentChapter++
            book.currentSection = 0
            clearContent()

            continueButton.text = NEXT_TEXT
            progressBar.max = book.chapters[book.currentChapter].content.size
        }

        val section = book.chapters[book.currentChapter].content[book.currentSection++]
        contentList.add(section)
        adapter.notifyItemInserted(contentList.size-1)

        section.onPlaySoundClick = {
            if (ttsReady && !section.isImage)
                tts.speak(section.content, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        section.onPlaySoundClick()

        linearLayoutManager.scrollToPosition(contentList.size)

        updateProgressBar()

        RecordKeeper.getInstance(this).setProgress(
            book.bookId,
            book.currentChapter,
            book.currentSection
        )

        if (book.currentChapter == book.chapters.size-1) {
            continueButton.text = NEXT_CHAPTER_TEXT
        }
    }
    private fun clearContent() {
        val previousContentSize = contentList.size
        contentList.clear()
        adapter.notifyItemRangeRemoved(0, previousContentSize)
    }

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
