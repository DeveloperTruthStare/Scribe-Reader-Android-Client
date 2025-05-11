package com.devilishtruthstare.scribereader.mian.library.bookicon

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.reader.Reader
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.collections.iterator

class BookView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val book: Book,
    useParentParent: Boolean = false,
    booksPerRow: Float = 2.0f
) : FrameLayout(context, attrs) {

    companion object {
        private const val MARGIN = 20
    }

    private val imageView: ImageView
    private val bookStatusImage: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.view_book_icon, this, true)
        imageView = findViewById(R.id.itemImage)
        bookStatusImage = findViewById(R.id.book_status)

        loadBookCover(book)
        val statusResource = when (book.status) {
            RecordKeeper.Companion.STATUS_NOT_STARTED -> R.drawable.play_arrow
            RecordKeeper.Companion.STATUS_IN_PROGRESS -> R.drawable.incomplete_circle
            RecordKeeper.Companion.STATUS_FINISHED -> R.drawable.check_circle
            else -> R.drawable.play_arrow
        }

        bookStatusImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, statusResource, null))

        setOnClickListener {
            val intent = Intent(context, Reader::class.java).apply {
                putExtra(Reader.Companion.EXTRA_BOOK_ID, book.bookId)
            }
            context.startActivity(intent)
        }

        setOnLongClickListener { view ->
            val popupView = BookDetailsView(context, book = book)
            val popupWindow = PopupWindow(
                popupView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.isOutsideTouchable = true
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

            true
        }

        post {
            adjustLayoutParams(useParentParent, booksPerRow)
        }
    }

    fun adjustLayoutParams(useParentParent: Boolean, booksPerRow: Float) {
        val parentLayout = if (useParentParent) {
            (parent?.parent as? ViewGroup)
        } else {
            parent as? ViewGroup
        } ?: return

        val parentWidth = parentLayout.width
        if (parentWidth == 0) return

        val totalPadding = parentLayout.paddingStart + parentLayout.paddingEnd
        val availableWidth = parentWidth - totalPadding - MARGIN * 2 * booksPerRow

        val imageWidth = availableWidth / booksPerRow
        val aspectRatio = imageView.drawable.intrinsicWidth.toFloat() / imageView.drawable.intrinsicHeight.toFloat()
        val imageHeight = imageWidth / aspectRatio

        layoutParams = LinearLayout.LayoutParams(imageWidth.toInt(), imageHeight.toInt()).apply {
            setMargins(MARGIN, MARGIN, MARGIN, MARGIN)
        }
        requestLayout()
    }

    private fun loadBookCover(book: Book) {
        val bitmap = getCoverImage(book.title)
        imageView.setImageBitmap(bitmap)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private fun getCoverImage(title: String): Bitmap {
        val file = File(context.filesDir, "books/$title/$title.epub")
        FileInputStream(file).use { inputStream ->
            val epubReader = EpubReader().readEpub(inputStream)
            val coverImage = findCoverResource(epubReader)
            return BitmapFactory.decodeStream(coverImage.inputStream)
        }
    }

    private fun findCoverResource(book: nl.siegmann.epublib.domain.Book) : Resource {
        if (book.coverImage != null) {
            return book.coverImage
        }

        for((_, resource) in book.resources.resourceMap) {
            if (File(resource.href).nameWithoutExtension == "cover") {
                return resource
            }
        }
        Log.d("CoverImage", "Could not find Cover Image")
        throw IOException("Could not find cover image")
    }
}