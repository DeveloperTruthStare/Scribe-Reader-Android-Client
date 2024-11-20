package com.devilishtruthstare.scribereader.library

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.ProgressBar
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.database.UserStats

class BookDetails(
    private val anchorView: View,
    private val context: Context,
    private val book: Book,
    private val downloaded: Boolean,
    private val downloader: DownloadedBooksTracker
) {
    companion object {
        private const val START_READING_TEXT = "読み始める"
        private const val CONTINUE_READING_TEXT = "つづく"
        private const val DOWNLOAD_TEXT = "ダウンロード"
    }

    private var onBookDownloaded: (() -> Unit)? = null
    private var onBookOpen: (() -> Unit)? = null
    private var onBookDeleted: (() -> Unit)? = null
    fun setOnBookDownloadedListener(listener: () -> Unit) {
        onBookDownloaded = listener
    }
    fun setOnOpenBookListener(listener: () -> Unit) {
        onBookOpen = listener
    }
    fun setOnBookDeletedListener(listener: () -> Unit) {
        onBookDeleted = listener
    }

    fun showDialog() {
        // Create a custom layout for the dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.book_details_popup, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val actionButton = dialogView.findViewById<Button>(R.id.actionButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.backButton)
        val deleteLocalButton = dialogView.findViewById<Button>(R.id.deleteLocalFiles)
        val resetProgressButton = dialogView.findViewById<Button>(R.id.resetProgress)
        val titleView = dialogView.findViewById<TextView>(R.id.bookTitle)

        titleView.text = book.title

        if (downloaded) {
            if (book.currentChapter > 0 || book.currentSection > 0) {
                resetProgressButton.visibility = View.VISIBLE
                actionButton.text = CONTINUE_READING_TEXT
            } else {
                actionButton.text = START_READING_TEXT
            }
        } else {
            deleteLocalButton.visibility = View.GONE
            actionButton.text = DOWNLOAD_TEXT
        }

        resetProgressButton.setOnClickListener {
            UserStats(context).resetStats(book.title)
        }

        // Create the Alert Dialog
        // Convert dp to pixels
        val marginWidth = (10 * context.resources.displayMetrics.density).toInt()

        // Get screen dimensions
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val popupWidth = screenWidth - (2 * marginWidth)

        val dialog = PopupWindow(dialogView, popupWidth, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        deleteLocalButton.setOnClickListener {
            // Delete from local files
            downloader.deleteLocalBook(book)
            dialog.dismiss()
            onBookDeleted?.invoke()
        }

        actionButton.setOnClickListener {
            if (downloaded) {
                onBookOpen?.invoke()
                dialog.dismiss()
            } else {
                cancelButton.visibility = View.GONE // Hide back button
                progressBar.visibility = View.VISIBLE // Show progress bar
                actionButton.visibility = View.GONE // Show "Downloading..." text

                // Start downloading and update progress
                downloader.downloadBook(book, onProgressUpdate = { progress ->
                    progressBar.progress = progress
                }, onDownloadComplete = {
                    onBookDownloaded?.invoke()
                    dialog.dismiss()
                })
            }
        }

        dialog.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }
}
