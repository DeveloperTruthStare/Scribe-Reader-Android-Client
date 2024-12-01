package com.devilishtruthstare.scribereader.library

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.R
import nl.siegmann.epublib.epub.EpubReader
import java.io.InputStream


class LibraryActivity : AppCompatActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        initializeFilePickerLauncher()

        findViewById<ImageView>(R.id.addToLibraryButton).setOnClickListener {
            filePickerLauncher.launch("application/epub+zip")
        }
    }

    // Function to process the .epub file
    private fun processEpubFile(uri: Uri) {
        val epubReader = EpubReader()
        val inputStream: InputStream? = contentResolver.openInputStream(Uri.parse(uri.toString()))
        val book = epubReader.readEpub(inputStream)
        if (book.metadata.language != "ja") {
            Log.d("LANG", "epub language is not japanese: ${book.metadata.language.toString()}");
        }

        // Add book to library
        // Copy file to internal directory
        // Yoink cover image and copy that separately
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