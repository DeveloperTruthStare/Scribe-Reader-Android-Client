package com.devilishtruthstare.scribereader.ui.reader

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
import com.devilishtruthstare.scribereader.book.utils.BookParser
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.ui.MainActivity
import com.devilishtruthstare.scribereader.ui.reader.content.BookContentAdapter
import kotlinx.coroutines.launch
import java.util.Locale


class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        const val EXTRA_BOOK_ID = "BOOK_ID"
        private const val NEXT_TEXT = "次"
        private const val NEXT_CHAPTER_TEXT = "次の第"
        private const val FINISH_BOOK_TEXT = "Finish Book"
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var titleText: TextView
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

        titleText = findViewById(R.id.reader_title)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        continueButton = findViewById(R.id.continue_button)
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            nextLineClick()
        }

        val recordKeeper = RecordKeeper.getInstance(this)
        book = recordKeeper.getBookById(bookId)!!
        titleText.text = book.title
        if (book.status == RecordKeeper.STATUS_NOT_STARTED) {
            recordKeeper.startBook(book.bookId)
        }
        recordKeeper.addOpenHistory(bookId)
        lifecycleScope.launch {
            book = BookParser.parseBook(this@Reader, book).await()
            for ((index, chapter) in book.chapters.withIndex()) {
                Log.d("ReaderActivity", "Chapter: ${index+1} Paragraphs: ${chapter.content.size}")
            }
            progressBar.max = book.chapters.size
            updateProgressBar()
            for(i in 0..book.currentSection) {
                val section = book.chapters[book.currentChapter].content[i]
                (!section.isImage).let {
                    section.onPlaySoundClick = {
                        if (ttsReady)
                            tts.speak(section.content, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
                contentList.add(section)
                adapter.notifyItemInserted(i)
            }
            continueButton.isEnabled = true
        }
    }

    private fun nextLineClick () {
        book.currentSection++
        if (book.currentSection >= book.chapters[book.currentChapter].content.size) {
            book.currentChapter++
            book.currentSection = 0
            clearContent()
            progressBar.max = book.chapters[book.currentChapter].content.size
            continueButton.text = NEXT_TEXT

            if (book.currentChapter >= book.chapters.size) {
                RecordKeeper.getInstance(this).finishBook(book.bookId)
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
        val nextSection = book.chapters[book.currentChapter].content[book.currentSection]
        contentList.add(nextSection)
        adapter.notifyItemInserted(contentList.size-1)

        if (!nextSection.isImage) {
            nextSection.onPlaySoundClick = {
                if (ttsReady)
                    tts.speak(nextSection.content, TextToSpeech.QUEUE_FLUSH, null, null)
            }
            nextSection.onPlaySoundClick()
        }

        linearLayoutManager.scrollToPosition(contentList.size)

        updateProgressBar()

        RecordKeeper.getInstance(this).setProgress(
            book.bookId,
            book.currentChapter,
            book.currentSection
        )

        if (book.currentSection == book.chapters[book.currentChapter].content.size-1) {
            if (book.currentChapter == book.chapters.size-1) {
                continueButton.text = FINISH_BOOK_TEXT
            } else {
                continueButton.text = NEXT_CHAPTER_TEXT
            }
        }
    }

    private fun updateProgressBar() {
        progressBar.progress = book.currentSection
        val text = "Section ${book.currentChapter+1}: ${book.currentSection+1}/${book.chapters[book.currentChapter].content.size}"
        progressText.text = text
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
