package com.devilishtruthstare.scribereader.ui.library

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.ui.library.bookicon.BookView

class AuthorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val bookContainer: LinearLayout
    private val authorText: TextView
    init {
        LayoutInflater.from(context).inflate(R.layout.view_library_author, this, true)

        authorText = findViewById(R.id.author_text)
        bookContainer = findViewById(R.id.book_container)
    }

    fun setAuthor(author: String) {
        authorText.text = author
    }

    fun addBook(book: Book) {
        val bookView = BookView(context, book = book, useParentParent = true, booksPerRow = 2.5f)
        bookContainer.addView(bookView)
        Log.d("AuthorView", "${bookContainer.childCount}")
    }
}