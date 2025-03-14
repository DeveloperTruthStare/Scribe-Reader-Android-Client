package com.devilishtruthstare.scribereader.library

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.reader.Reader
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class LibraryActivity : AppCompatActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val recordKeeper = RecordKeeper(this)

    private lateinit var libraryContainer: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        libraryContainer = findViewById(R.id.libraryContainer)

        initializeFilePickerLauncher()

        findViewById<ImageView>(R.id.addToLibraryButton).setOnClickListener {
            filePickerLauncher.launch("application/epub+zip")
        }

        refreshLibraryView()
        val outputDir = File(filesDir, "books")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    private fun refreshLibraryView() {
        libraryContainer.removeAllViews()

        val books = recordKeeper.getBookList()

        for(book in books) {
            // Create a book icon
            val bookIcon = createBookIcon(book)

            // Add it to the view container
            libraryContainer.addView(bookIcon)
        }
    }

    private fun createBookIcon(book: Book) : View? {
        val file = File(filesDir, "books/${book.title}/${book.title}.epub")
        val inputStream = FileInputStream(file)
        val epubReader = EpubReader().readEpub(inputStream)
        val coverImage = findCoverPageResource(epubReader)

        val bookIconView = LayoutInflater.from(this)
            .inflate(R.layout.book_icon, libraryContainer, false)

        // Set the Book Cover Image
        val imageView = bookIconView.findViewById<ImageView>(R.id.itemImage)
        var bitmap: Bitmap? = null

        coverImage.let {
            val imageInputStream: InputStream = it.inputStream
            bitmap = BitmapFactory.decodeStream(imageInputStream)
            imageView.setImageBitmap(bitmap)
        }

        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        // Set the action for on click
        bookIconView.setOnClickListener {
            openBook(book.title)
        }

        bookIconView.setOnLongClickListener {
            true
        }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        val aspectRatio = bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
        val calculatedHeight = (screenWidth / 2 - 45) / aspectRatio


        bookIconView.layoutParams = GridLayout.LayoutParams().apply {
            width = screenWidth / 2 - 45
            height = calculatedHeight.toInt()
            setMargins(10, 10, 10, 10)
        }
        return bookIconView
    }

    private fun openBook(title: String) {
        // Grab the book from the records to get the correct progress
        val book = recordKeeper.getBook(title)!!
        val intent = Intent(this, Reader::class.java)
        intent.putExtra("EXTRA_TITLE", book.title)
        intent.putExtra("EXTRA_CHAPTER", book.currentChapter)
        intent.putExtra("EXTRA_SECTION", book.currentSection)
        startActivity(intent)
    }

    // Functions to process the .epub file
    private fun processEpubFile(uri: Uri) {
        val epubReader = EpubReader()
        val inputStream = contentResolver.openInputStream(uri) ?: throw IOException("Failed to open input stream")
        val book = epubReader.readEpub(inputStream)
        if (book.metadata.language != "ja") {
            Log.d("LANG", "epub language is not japanese: ${book.metadata.language}");
        }

        if (recordKeeper.getBook(book.title) != null) {
            Log.d("RecordKeeper", "Book already exists")
            return
        }

        val outputDir = File(filesDir, "books/${book.title}")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // Copy to internal directory
        val rBook = Book()
        rBook.title = book.title
        rBook.fileLocation = "books/${book.title}/${book.title}.epub"

        copyFileToDestination(contentResolver, uri, rBook.fileLocation)

        recordKeeper.onBookDownloaded(rBook)

        // Refresh library view
        refreshLibraryView()
    }

    private fun findCoverPageResource(book: nl.siegmann.epublib.domain.Book) : Resource {
        if (book.coverImage != null) {
            return book.coverImage
        }

        for((_, resource) in book.resources.resourceMap) {
            if (resource.href.contains("cover"))
                return resource
        }
        throw IOException("Could not find cover image")
    }

    private fun copyFileToDestination(contentResolver: ContentResolver, uri: Uri, destination: String) {
        try {
            // Get the InputStream from the URI
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Failed to open input stream from Uri")

            // Create a destination file
            val destinationFile = File(filesDir, destination)

            // Create an OutputStream to the destination file
            val outputStream = FileOutputStream(destinationFile)

            // Copy the content from the InputStream to the OutputStream
            copyStream(inputStream, outputStream)

            // Close streams
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper function to copy data from InputStream to OutputStream
    private fun copyStream(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    }

    private fun initializeFilePickerLauncher() {
        filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // This is where the app receives the selected file URI
                Log.d("FileSelector", "Selected file: $uri")

                // Process the selected file
                processEpubFile(uri)
            } else {
                Log.d("FileSelector", "No file selected")
            }
        }
    }
}