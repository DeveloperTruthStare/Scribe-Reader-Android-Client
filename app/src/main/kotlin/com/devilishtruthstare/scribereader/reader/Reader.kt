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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.database.UserStats
import com.devilishtruthstare.scribereader.reader.content.BookContentAdapter
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.InputStream
import java.util.Locale
import java.io.FileInputStream


class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        private const val NEXT_TEXT = "次"
        private const val NEXT_CHAPTER_TEXT = "次の第"
    }


    private lateinit var bookParser: BookParser

    private lateinit var recyclerView: RecyclerView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var continueButton: Button

    private lateinit var adapter: BookContentAdapter
    private lateinit var contentList: MutableList<Content>

    private lateinit var userStats: UserStats

    private lateinit var tts: TextToSpeech
    private var ttsReady: Boolean = false

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
        val title = intent.getStringExtra("EXTRA_TITLE")

        // Load Ebook into memory
        val epubReader = EpubReader()
        val inputStream: InputStream = FileInputStream(File(filesDir, "books/${title}/${title}.epub"))

        bookParser = BookParser(epubReader.readEpub(inputStream), tokenizer.Tokenizer.newTokenizer())
        bookParser.processBook()

        bookParser.setChapter(intent.getIntExtra("EXTRA_CHAPTER", 0))
        bookParser.setSection(intent.getIntExtra("EXTRA_SECTION", 0))

        userStats = UserStats(this)
        contentList = mutableListOf()

        // Setup the Continue Button
        continueButton = findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            nextLineClick()
        }

        // Setup the Recycler View
        recyclerView = findViewById(R.id.recycler_view)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = BookContentAdapter(contentList, this)
        recyclerView.adapter = adapter

        linearLayoutManager.scrollToPosition(contentList.size)


        // Set Progress Initial Values
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)
        progressBar.max = bookParser.getChapterSize()
        updateProgressBar()
    }

    private fun updateProgressBar() {
        progressBar.progress = bookParser.getCurrentSection()
        progressBar.max = bookParser.getChapterSize()

        val text = "Chapter: ${bookParser.getCurrentChapter()}: ${bookParser.getCurrentSection()+1}/${bookParser.getChapterSize()}"
        progressText.text = text
    }

    private fun nextLineClick () {
        if (bookParser.endOfChapter()) {
            if (bookParser.endOfBook()) {
                Log.e("TODO", "Book finished")
                // Intent go to library activity
                return
            }

            bookParser.nextChapter()
            clearContent()

            continueButton.text = NEXT_TEXT
            progressBar.max = bookParser.getChapterSize()
        }

        val section = bookParser.nextSection()
        contentList.add(section)
        adapter.notifyItemInserted(contentList.size-1)

        if (ttsReady && !section.isImage)
            tts.speak(section.content, TextToSpeech.QUEUE_FLUSH, null, null)

        linearLayoutManager.scrollToPosition(contentList.size)

        updateProgressBar()

        userStats.setCurrentPage(bookParser.getTitle(), bookParser.getCurrentChapter(), bookParser.getCurrentSection())

        if (bookParser.endOfChapter()) {
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
        // Shutdown TTS when the activity is destroyed
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
