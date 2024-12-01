package com.devilishtruthstare.scribereader.reader

import android.content.Intent
import android.net.Uri
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
import com.devilishtruthstare.scribereader.ScribeReaderApp
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.database.UserStats
import com.devilishtruthstare.scribereader.reader.content.BookContentAdapter
import com.github.wanasit.kotori.Tokenizer
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.InputStream
import java.util.Locale
import org.jsoup.Jsoup
import org.jsoup.nodes.Element



class Reader : AppCompatActivity(), OnInitListener {
    companion object {
        private const val NEXT_TEXT = "次"
        private const val NEXT_CHAPTER_TEXT = "次の第"
    }

    private var currentChapter: Int = 0
    private var currentSection: Int = 1
    private var numChapters: Int = 0
    private lateinit var book: nl.siegmann.epublib.domain.Book

    private var sections: MutableList<Content> = mutableListOf()
    private var imageMap: MutableMap<String, File> = mutableMapOf()

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

    private lateinit var tokenizer: Tokenizer<DefaultTermFeatures>

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

        val bookUri = intent.getStringExtra("EXTRA_BOOK_URI")
        currentChapter = intent.getIntExtra("EXTRA_CHAPTER", 0)
        currentSection = intent.getIntExtra("EXTRA_SECTION", 0)

        // Load Ebook into memory
        val epubReader = EpubReader()

        val inputStream: InputStream? = contentResolver.openInputStream(Uri.parse(bookUri))
        book = epubReader.readEpub(inputStream)

        imageMap = mutableMapOf()

        val resources = book.spine.spineReferences.map { it.resource }
        val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
        for(resource in resources) {
            if (resource.mediaType.defaultExtension in imageExtensions) {
                val tempFile = File.createTempFile("image_", ".${resource.mediaType.defaultExtension}")
                tempFile.outputStream().use { it.write(resource.data) }
                imageMap[resource.href] = tempFile
            }
        }

        numChapters = book.resources.size()

        processChapter()

        // Initialize the Japanese text Tokenizer
        tokenizer = (application as ScribeReaderApp).tokenizer


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
        progressBar.max = sections.size
        updateProgressBar()
    }

    private fun processChapter() {
        sections = mutableListOf()
        val resources = book.spine.spineReferences.map { it.resource }

        var skip: Boolean
        var body: Element

        do {
            skip = false

            val text = resources[currentChapter].reader.readText().trim()
            val document = Jsoup.parse(text)

            body = document.body()

            for (className in body.classNames()) {
                if (className == "p-caution" || className == "p-colophon") {
                    skip = true
                    break
                }
            }
        } while (skip)

        traverseNodes(body)

        if (sections.size == 0) {
            currentChapter++
            processChapter()
        }
    }

    private fun traverseNodes(element: Element, depth: Int = 0) {
        if (element.tagName() == "p") {
            val result = StringBuilder()

            // Iterate over all child nodes of the <p> tag
            for (node in element.childNodes()) {
                when {
                    node.nodeName() == "ruby" -> {
                        val rubyElement = node as Element
                        result.append(rubyElement.ownText())
                    }
                    node.nodeName() == "#text" -> {
                        result.append(node.toString())
                    }
                    node.nodeName() == "img" || node.nodeName() == "image" -> {
                        val imgElement = node as Element
                        val src = imgElement.attr("src")
                        val href = imgElement.attr("href")
                        if (src.isNotBlank()) {
                            sections.add(createImageSection(src))
                        } else if (href.isNotBlank()) {
                            sections.add(createImageSection(href))
                        }
                    }
                }
            }
            if (result.trim() != "") {
                sections.add(createTextSection(result.toString().trim()))
            }
        }

        for (child in element.children()) {
            traverseNodes(child, depth + 1)
        }
    }
    private fun createImageSection(src: String): Content {

        return Content(
            isImage = true,
            text = "",
            imageUrl = src,
            tokens = emptyList(),
            imageFile = imageMap[src]!!,
            onPlaySoundClick = { }
        )
    }
    private fun createTextSection(text: String): Content {
        val tokens = tokenizer.tokenize(text)

        return Content(
            isImage = false,
            text = text,
            imageUrl = "",
            tokens = tokens,
            imageFile = createTempFile("temp_", "file"),
            onPlaySoundClick = { }
        )
    }

    private fun updateProgressBar() {
        progressBar.progress = currentSection
        val text = "Chapter: $currentChapter: ${currentSection+1}/${sections.size}"
        progressText.text = text
    }

    private fun addContent(position: Int) : Content {
        val content = generateContent(position)
        contentList.add(content)
        adapter.notifyItemInserted(contentList.size-1)
        return content
    }

    private fun generateContent(line: Int) : Content {
        val section = sections[line]
        if (section.isImage) {
            section.imageFile = File(filesDir, "books/$title/images/${section.imageUrl}")
        }
        return section
    }

    private fun nextLineClick () {
        if (currentSection == sections.size) {
            if (currentChapter == numChapters) {
                Log.e("Reader", "Book finished")
                return
            }
            currentChapter++
            currentSection = 0
            clearContent()
            processChapter()
            continueButton.text = NEXT_TEXT
            progressBar.max = sections.size
        }

        val section = addContent(currentSection)
        if (ttsReady)
            tts.speak(section.text, TextToSpeech.QUEUE_FLUSH, null, null)


        linearLayoutManager.scrollToPosition(contentList.size)

        updateProgressBar()

        currentSection++
        userStats.setCurrentPage(book.title, currentChapter, currentSection)

        if (currentSection == sections.size) {
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