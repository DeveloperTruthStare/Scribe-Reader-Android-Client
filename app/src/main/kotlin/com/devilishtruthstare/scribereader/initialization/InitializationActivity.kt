package com.devilishtruthstare.scribereader.initialization

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.devilishtruthstare.scribereader.MainActivity
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.dictionary.DictionaryUtils
import kotlinx.coroutines.launch


class InitializationActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_initialization)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.progressBar)

        loadDictionary {
            moveToLibraryActivity()
        }
    }

    private fun loadDictionary(onComplete: () -> Unit) {
        val dictionary = JMDict.getInstance(this)

        if (!dictionary.isDatabaseInitialized()) {
            val inputStream = resources.openRawResource(R.raw.jmdict)
            val entries = DictionaryUtils.parseJson(inputStream)
            var entriesAdded = 0
            val numEntries = entries.size
            progressBar.max = numEntries
            lifecycleScope.launch {
                dictionary.loadEntries(entries, onEntryAdded = {
                    runOnUiThread {
                        entriesAdded += 100
                        val percent = (entriesAdded.toFloat() / numEntries * 100).toInt()
                        statusText.text = getString(R.string.initialization_loading_jmdict, percent)
                        progressBar.progress = entriesAdded
                    }
                }, onFinished = {
                    onComplete()
                })
            }
        } else {
            onComplete()
        }
    }
    private fun moveToLibraryActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}
