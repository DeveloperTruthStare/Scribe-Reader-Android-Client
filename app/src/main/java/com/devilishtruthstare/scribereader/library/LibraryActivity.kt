package com.devilishtruthstare.scribereader.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.reader.Reader


class LibraryActivity : AppCompatActivity() {

    private lateinit var filePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

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
        findViewById<Button>(R.id.openButton).setOnClickListener {
            openFileSelector()
        }
    }

    // Function to open the file selector
    private fun openFileSelector() {
        filePickerLauncher.launch("application/epub+zip")
    }

    // Function to process the .epub file
    private fun processEpubFile(uri: Uri) {
        val intent = Intent(this, Reader::class.java)
        intent.putExtra("EXTRA_BOOK_URI", uri.toString())
        startActivity(intent)
    }
}