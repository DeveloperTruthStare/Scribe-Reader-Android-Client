package com.devilishtruthstare.scribereader.library

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.reader.Reader
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BookView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val book: Book? = null
) : FrameLayout(context, attrs) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_book_icon, this, true)

        val file = File(context.filesDir, "books/${book!!.title}/${book.title}.epub")
        val inputStream = FileInputStream(file)
        val epubReader = EpubReader().readEpub(inputStream)
        val coverImage = findCoverResource(epubReader)

        val imageView = findViewById<ImageView>(R.id.itemImage)
        val bitmap = BitmapFactory.decodeStream(coverImage.inputStream)
        imageView.setImageBitmap(bitmap)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP

        setOnClickListener {
            val intent = Intent(context, Reader::class.java)
            intent.putExtra(Reader.EXTRA_BOOK_ID, book.bookId)
            context.startActivity(intent)
        }
        setOnLongClickListener {
            true
        }

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val aspectRatio = bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
        val calculateHeight = (screenWidth / 2 - 45) / aspectRatio

        layoutParams = GridLayout.LayoutParams().apply {
            width = screenWidth / 2 - 45
            height = calculateHeight.toInt()
            setMargins(10, 10, 10, 10)
        }
    }

    private fun findCoverResource(book: nl.siegmann.epublib.domain.Book) : Resource {
        if (book.coverImage != null) {
            return book.coverImage
        }

        for((_, resource) in book.resources.resourceMap) {
            if (resource.href.contains("cover"))
                return resource
        }
        throw IOException("Could not find cover image")
    }
}