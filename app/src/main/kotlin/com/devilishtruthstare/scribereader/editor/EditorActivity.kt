package com.devilishtruthstare.scribereader.editor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.R

class EditorActivity : AppCompatActivity() {
    private lateinit var textEditor: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editor_activity)

        if (intent.action != Intent.ACTION_VIEW) {
            return
        }
        val uri: Uri = intent.data ?: return

        linkUI()

        handleFile(uri)
    }

    private fun linkUI() {
        textEditor = findViewById(R.id.editor_main)
    }

    private fun handleFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val contents = inputStream?.bufferedReader()?.use { it.readText() }
        textEditor.setText(contents)
    }
}