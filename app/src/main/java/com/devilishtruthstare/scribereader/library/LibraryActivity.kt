package com.devilishtruthstare.scribereader.library

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.reader.Reader
import java.io.File


class LibraryActivity : AppCompatActivity() {
    private lateinit var bookShelfLayout: GridLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var bookDownloader: DownloadedBooksTracker
    private lateinit var recordKeeper: RecordKeeper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        recordKeeper = RecordKeeper(this)
        bookDownloader = DownloadedBooksTracker(this, recordKeeper)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        bookShelfLayout = findViewById(R.id.gridLayout)

        swipeRefreshLayout.setOnRefreshListener {
            downloadBookList()
        }
        loadLocalBooks()
        downloadBookList()
    }

    private fun downloadBookList() {
        bookDownloader.downloadBookList(onFailure = { _, e ->
            runOnUiThread {
                Log.e("HTTP", "IOException occurred: ${e.message}", e)
                swipeRefreshLayout.isRefreshing = false
                loadLocalBooks()
            }
        }, onSuccess = { library ->
            runOnUiThread {
                val books = recordKeeper.getBookList().toMutableList()
                for (book in library.books) {
                    var containsBook = false
                    for (link in books) {
                        if (link.title == book.title) {
                            containsBook = true
                            break
                        }
                    }
                    if (!containsBook) {
                        books.add(book)
                    }
                }
                addItemsToGridLayout(books)
                swipeRefreshLayout.isRefreshing = false
            }
        })
    }

    private fun loadLocalBooks() {
        val books = recordKeeper.getBookList()
        addItemsToGridLayout(books)
    }

    private fun createBookIcon(book: Book) : View? {
        val bookIconView = LayoutInflater.from(this)
            .inflate(R.layout.book_icon, bookShelfLayout, false)

        // Set the Book Cover Image
        val imageView = bookIconView.findViewById<ImageView>(R.id.itemImage)
        val coverFile = File(filesDir, "books/${book.title}/images/${book.coverImage}")
        if (coverFile.exists()) {
            Glide.with(bookIconView.context).load(coverFile).into(imageView)
        } else {
            Glide.with(bookIconView.context)
                .load("${DownloadedBooksTracker.BASE_URI}/getcover?title=${book.title}")
                .into(imageView)
        }

        // Set the action for on click
        bookIconView.setOnClickListener {
            if (recordKeeper.hasBook(book.title)) {
                openBook(book.title)
            } else {
                showDownloadDialogue(bookIconView, book, false)
            }
        }

        bookIconView.setOnLongClickListener {
            showDownloadDialogue(bookIconView, book, true)
            true
        }
        bookIconView.layoutParams = GridLayout.LayoutParams().apply {
            width = GridLayout.LayoutParams.WRAP_CONTENT
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(10, 10, 10, 10)
        }

        return bookIconView
    }

    private fun showDownloadDialogue(anchorView: View, book: Book, bookDownloaded: Boolean) {
        val dialog = BookDetails(anchorView, this, book, bookDownloaded, bookDownloader)
        dialog.setOnOpenBookListener {
            openBook(book.title)
        }
        dialog.setOnBookDownloadedListener {
            openBook(book.title)
            downloadBookList()
        }
        dialog.setOnBookDeletedListener {
            downloadBookList()
        }
        dialog.showDialog()
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
    private fun addItemsToGridLayout(items: List<Book>) {
        bookShelfLayout.removeAllViews()
        for (item in items) {
            val bookIcon = createBookIcon(item)
            bookShelfLayout.addView(bookIcon)
        }
    }
}