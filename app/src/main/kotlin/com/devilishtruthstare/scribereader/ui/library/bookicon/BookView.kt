package com.devilishtruthstare.scribereader.ui.library.bookicon

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.core.content.res.ResourcesCompat
import com.devilishtruthstare.scribereader.ImageLoader
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.reader.Reader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        loadBookCoverAsync(book)
        val statusResource = when (book.status) {
            RecordKeeper.STATUS_NOT_STARTED -> R.drawable.play_arrow
            RecordKeeper.STATUS_IN_PROGRESS -> R.drawable.incomplete_circle
            RecordKeeper.STATUS_FINISHED -> R.drawable.check_circle
            else -> R.drawable.play_arrow
        }

        bookStatusImage.setImageDrawable(ResourcesCompat.getDrawable(context.resources, statusResource, null))

        setOnClickListener {
            val intent = Intent(context, Reader::class.java).apply {
                putExtra(resources.getString(R.string.EXTRA_BOOK_ID), book.bookId)
            }
            context.startActivity(intent)
        }

        setOnLongClickListener { view ->
            // Convert dp to pixels
            val marginWidth = (20 * context.resources.displayMetrics.density).toInt()

            // Get screen dimensions
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Calculate popup dimensions
            val popupWidth = screenWidth - (2 * marginWidth)  // Full width minus margins
            val popupHeight = screenHeight * 3 / 4

            val popupView = BookDetailsView(context, book = book)
            val popupWindow = PopupWindow(
                popupView,
                popupWidth,
                popupHeight,
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

    private var useParentParent: Boolean = false
    private var booksPerRow: Float = 2.0f
    fun adjustLayoutParams(useParentParent: Boolean, booksPerRow: Float) {
        this.useParentParent = useParentParent
        this.booksPerRow = booksPerRow
        val parentLayout = if (useParentParent) {
            (parent?.parent as? ViewGroup)
        } else {
            parent as? ViewGroup
        } ?: return
        if (imageView.drawable == null) {
            return
        }

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
    private fun loadBookCoverAsync(book: Book) {
        CoroutineScope(Dispatchers.IO).launch {
            val bitmap = ImageLoader.getInstance(context).getCoverImage(book.title)
            withContext(Dispatchers.Main) {
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                adjustLayoutParams(useParentParent, booksPerRow)
            }
        }
    }
}