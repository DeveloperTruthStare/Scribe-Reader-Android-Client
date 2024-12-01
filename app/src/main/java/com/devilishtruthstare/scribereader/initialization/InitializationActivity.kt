package com.devilishtruthstare.scribereader.initialization

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.ScribeReaderApp
import com.devilishtruthstare.scribereader.jmdict.JMDict
import com.devilishtruthstare.scribereader.jmdict.JMDictParser
import com.devilishtruthstare.scribereader.library.LibraryActivity
import com.github.wanasit.kotori.Tokenizer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InitializationActivity : AppCompatActivity() {
    companion object {
        private const val STATUS_LOAD_JMDICT = "Loading JMDict"
        private const val STATUS_LOADING_DATABASE = "Loading Entries into Database"
        private const val STATUS_CHECKING_DB = "Checking Database"
        private const val STATUS_INITIALIZING_TOKENIZER = "Initializing Tokenizer"
    }
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var secondaryProgressText: TextView

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
        progressText = findViewById(R.id.progressText)
        secondaryProgressText = findViewById(R.id.secondaryProgressText)

        loadTokenizer {
            loadDictionary {
                moveToLibraryActivity()
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun loadTokenizer(onComplete: () -> Unit) {
        statusText.text = STATUS_INITIALIZING_TOKENIZER
        progressBar.max = 100
        progressBar.progress = 1

        GlobalScope.launch(Dispatchers.Main) {
            // Call the background operation on IO dispatcher
            withContext(Dispatchers.IO) {
                (application as ScribeReaderApp).tokenizer = Tokenizer.createDefaultTokenizer()
                runOnUiThread {
                    onComplete()
                }
            }
        }
    }
    private fun loadDictionary(onComplete: () -> Unit) {
        val dictionary = JMDict(this)

        // Check if JMDict is already loaded into sqlite
        statusText.text = STATUS_CHECKING_DB
        if (!dictionary.isDatabaseInitialized()) {
            // Load JMDict
            statusText.text = STATUS_LOAD_JMDICT
            progressText.text = "1 / 2"
            val inputStream = resources.openRawResource(R.raw.jmdict)
            val entries = JMDictParser().parseJson(inputStream)
            // Enter entries into SQLite
            statusText.text = STATUS_LOADING_DATABASE
            progressText.text = "2 / 2"
            var entriesAdded = 0
            val numEntries = entries.size
            progressBar.max = numEntries
            val secondText = "0 / $numEntries"
            secondaryProgressText.text = secondText
            dictionary.loadEntries(entries, onEntryAdded = {
                runOnUiThread {
                    entriesAdded++
                    val text = "$entriesAdded / $numEntries"
                    secondaryProgressText.text = text
                    progressBar.progress = entriesAdded
                }
            }, onFinished = {
                onComplete()
            })
        } else {
            onComplete()
        }
    }
    private fun moveToLibraryActivity() {
        // Move to main screen
        val intent = Intent(this, LibraryActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish() // Optional, explicitly finishes the InitializationActivity
    }
}
