package com.devilishtruthstare.scribereader.ui.uploader

import android.app.AlertDialog
import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.utils.BookParser
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.dictionary.DictionaryUtils
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.ui.library.LibraryFragment
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.collections.iterator

class UploadFragment : Fragment(R.layout.fragment_uploader) {
    private lateinit var recordKeeper: RecordKeeper
    private lateinit var bookUri: Uri
    private lateinit var book: Book
    private lateinit var author: Author
    private lateinit var dialog: AlertDialog
    private lateinit var dialogProgressBar: ProgressBar
    private lateinit var dialogProgressTitle: TextView

    fun setBook(bookUri: Uri) {
        this.bookUri = bookUri
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recordKeeper = RecordKeeper(requireContext())

        val epubReader = EpubReader()
        val inputStream = requireContext().contentResolver.openInputStream(bookUri)
            ?: throw IOException("Failed to open input stream")
        book = epubReader.readEpub(inputStream)
        if (recordKeeper.getBook(book.title) != null) {
            navigateToLibrary()
            return
        }

        val coverImage = findCoverResource(book)

        val imageView = view.findViewById<ImageView>(R.id.bookCover)
        val bitmap = BitmapFactory.decodeStream(coverImage.inputStream)
        imageView.setImageBitmap(bitmap)

        imageView.post {
            val aspectRatio = bitmap?.let { it.width.toFloat() / it.height.toFloat() } ?: 1f
            val imageHeight = imageView.width.toFloat() / aspectRatio
            imageView.layoutParams.height = imageHeight.toInt()
            imageView.requestLayout()
        }


        author = book.metadata.authors[0]
        val authorDisplay = "${author.lastname} ${author.firstname}"

        view.findViewById<TextView>(R.id.bookTitle).text = book.title
        view.findViewById<TextView>(R.id.author).text = authorDisplay
        view.findViewById<TextView>(R.id.language).text = book.metadata.language

        view.findViewById<Button>(R.id.addToLibraryButton).setOnClickListener {
            processBook()
        }

    }
    private fun navigateToLibrary() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LibraryFragment())
            .commit()
    }

    private fun processBook() {
        showProcessingDialog()

        val outputDir = File(requireContext().filesDir, "books/${book.title}")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // Copy to internal directory
        var rBook = com.devilishtruthstare.scribereader.book.Book().apply {
            title = book.title
            author = "${book.metadata.authors.firstOrNull()?.lastname ?: ""} ${book.metadata.authors.firstOrNull()?.firstname ?: ""}"
            language = book.metadata.language
            fileLocation = "books/${book.title}/${book.title}.epub"
        }


        copyFileToDestination(requireContext().contentResolver, bookUri, rBook.fileLocation)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            rBook = BookParser.parseBook(requireContext(), rBook).await()
            var totalTokens = rBook.chapters.sumOf { it.content.size }

            requireActivity().runOnUiThread {
                dialogProgressBar.max = totalTokens
                dialogProgressTitle.text = getString(R.string.uploading_processing_book)
            }

            var tokensAdded = 0
            setProgress(tokensAdded, totalTokens)
            JMDict.getInstance(requireContext()).insertBook(
                rBook,
                onTokenAdded = {
                    tokensAdded++
                    setProgress(tokensAdded, totalTokens)
                },
                onFinished = {
                    recordKeeper.onBookDownloaded(rBook)
                    requireActivity().runOnUiThread {
                        dialog.dismiss()
                        navigateToLibrary()
                    }
                }
            )
        }
    }

    private fun setProgress(current: Int, max: Int) {
        requireActivity().runOnUiThread {
            dialogProgressBar.progress = current
            val percent = (current.toFloat() / max * 100).toInt()
            dialogProgressTitle.text = getString(R.string.uploading_processing_book, percent)
        }
    }
    private fun copyFileToDestination(contentResolver: ContentResolver, uri: Uri, destination: String) {
        try {
            // Get the InputStream from the URI
            val inputStream = contentResolver.openInputStream(uri)
                ?: throw IOException("Failed to open input stream from Uri")

            // Create a destination file
            val destinationFile = File(requireContext().filesDir, destination)

            // Create an OutputStream to the destination file
            val outputStream = FileOutputStream(destinationFile)

            // Copy the content from the InputStream to the OutputStream
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()

            // Close streams
            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProcessingDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_processing_book, null)
        builder.setView(view)
        builder.setCancelable(false)

        dialogProgressBar = view.findViewById(R.id.dialogProgressBar)
        dialogProgressTitle = view.findViewById(R.id.dialogTitle)

        dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
    private fun findCoverResource(book: Book) : Resource {
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