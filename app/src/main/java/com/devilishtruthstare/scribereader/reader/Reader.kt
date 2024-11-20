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
import com.google.gson.Gson
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.database.UserStats
import com.devilishtruthstare.scribereader.reader.content.BookContentAdapter
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.util.Locale
import kotlin.math.max


class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        private const val NEXT_TEXT = "次"
        private const val NEXT_CHAPTER_TEXT = "次の第"
    }
    private lateinit var book: Book
    private lateinit var title: String
    private var currentChapter: Int = 0
    private var currentSection: Int = 1

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
        title = intent.getStringExtra("EXTRA_TITLE")!!
        currentChapter = intent.getIntExtra("EXTRA_CHAPTER", 0)
        currentSection = intent.getIntExtra("EXTRA_SECTION", 1)
        currentSection = max(currentSection, 1)

        userStats = UserStats(this)
        contentList = mutableListOf()

        // Setup the Continue Button
        continueButton = findViewById(R.id.continue_button)
        continueButton.setOnClickListener {
            nextLineClick()
        }

        // Load the book into memory
        val epubReader = EpubReader()
        val book = epubReader.readEpub(FileInputStream(File(filesDir, "books/$title")))

        val resources = book.spine.spineReferences.map { it.resource }
        for (resource in resources) {
            val content = resource.reader.use { it.readText() }
            val sentences = content.split(Regex("(?<=[.!?])\\s+"))

            for (sentence in sentences) {
                Log.d("Sentence: ", sentence.trim())
            }
        }
    }

    fun temp() {
        val jsonString = findDataFile()
        val gson = Gson()
        book = gson.fromJson(jsonString, Book::class.java)!!

        // Setup the initial content list
        for (i in 0 until currentSection) {
            val content = generateContent(currentChapter, i)
            contentList.add(content)
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
        progressBar.max = book.chapters[currentChapter].content.size
        updateProgressBar()
    }

    private fun updateProgressBar() {
        progressBar.progress = currentSection
        val text = "Chapter: $currentChapter: ${currentSection+1}/${book.chapters[currentChapter].content.size}"
        progressText.text = text
    }

    private fun addContent(position: Int) : Content {
        val content = generateContent(currentChapter, position)
        contentList.add(content)
        adapter.notifyItemInserted(contentList.size-1)
        return content
    }

    private fun generateContent(chapter: Int, line: Int) : Content {
        val section = book.chapters[chapter].content[line]
        if (section.isImage) {
            section.imageFile = File(filesDir, "books/$title/images/${section.imageUrl}")
        }
        return section
    }

    private fun nextLineClick () {
        if (currentSection == book.chapters[currentChapter].content.size) {
            if (currentChapter == book.chapters.size-1) {
                Log.e("Reader", "Book finished")
                return
            }
            currentChapter++
            currentSection = 0
            clearContent()
            continueButton.text = NEXT_TEXT
            progressBar.max = book.chapters[currentChapter].content.size
        }

        val section = addContent(currentSection)
        tts.speak(section.text, TextToSpeech.QUEUE_FLUSH, null, null)


        linearLayoutManager.scrollToPosition(contentList.size)

        updateProgressBar()

        currentSection++
        userStats.setCurrentPage(book.title, currentChapter, currentSection)

        if (currentSection == book.chapters[currentChapter].content.size) {
            continueButton
            continueButton.text = NEXT_CHAPTER_TEXT
        }
    }
    private fun clearContent() {
        val previousContentSize = contentList.size
        contentList.clear()
        adapter.notifyItemRangeRemoved(0, previousContentSize)
    }

    private fun findDataFile(): String? {
        // Define the root directory where we will search
        val dir = File(filesDir, "books")

        // Check if the directory exists and is a directory
        if (!dir.exists() || !dir.isDirectory) {
            Log.e("READER", "Directory not found: ${dir.absolutePath}")
            return null
        }

        // Recursively search through all files in the directory
        val dataFile = File(dir, "$title/data.json")

        if (dataFile.exists()) {
            Log.d("READER", "Found data.json at: ${dataFile.absolutePath}")
            return dataFile.readText()
        } else {
            Log.e("READER", "data.json file not found in directory: ${dir.absolutePath}")
            return null
        }
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