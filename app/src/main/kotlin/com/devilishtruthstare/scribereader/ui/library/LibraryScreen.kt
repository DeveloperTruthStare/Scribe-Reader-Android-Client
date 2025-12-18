package com.devilishtruthstare.scribereader.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.components.CoverImage
import com.devilishtruthstare.scribereader.ui.reader.Reader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile

@Composable
fun LibraryScreen() {
    val context = LocalContext.current
    var library by remember { mutableStateOf(getLibrary(context)) }
    var filesList by remember { mutableStateOf(mutableListOf<String>()) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        LazyColumn {
            library.entries.forEach { entry ->
                item {
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                item {
                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        items(entry.value) { book ->
                            CoverImage(book.title) {
                                val intent = Intent(context, Reader::class.java).apply {
                                    putExtra(
                                        context.resources.getString(R.string.EXTRA_BOOK_ID),
                                        book.bookId
                                    )
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }

            items(filesList) { item ->
                Text(text = item)
            }

        }

        FilePickerScreen { uri ->
            // Copy the file to application storage
            copyFileToDestination(context, uri, "/cache/book.epub")

            val bookFile = File(context.filesDir, "/cache/book.epub")
            filesList = listFiles(bookFile).toMutableList()

        }
    }
}



data class File(
    val filename: String,
    val fileType: String
)

fun listFiles(epubFile: File): List<String> {
    ZipFile(epubFile).use { zip ->
        return zip.entries().asSequence().map { it.name }.toList()
    }
}

class Book {
    private var initialized = false
    private var files = mutableListOf<File>()
    var title: String = ""
    constructor() {

    }
    constructor(epubFile: File) {
        /*ZipFile(epubFile).use { zip ->
            var entry: ZipEntry? = zip.nextEntry
            while (entry != null) {
                val name = entry.name
                Log.d("ZipFileExtractor", name)
                when {
                    name == "mimetype" -> {
                        val mimetype = zip.bufferedReader().readText()
                        Log.d("Minetype", mimetype)
                    }
                    name == "META-INF/container.xml" -> {
                        val xml = zip.bufferedReader().readText()
                        Log.d("ZipFile: ", name)
                    }
                    name.endsWith(".opf", ignoreCase = true) -> {
                        val opf = zip.bufferedReader().readText()
                        Log.d("OPF: ", name)
                    }
                    name.endsWith(".xhtml", ignoreCase = true) ||
                    name.endsWith(".html", ignoreCase = true) -> {
                        val html = zip.bufferedReader().readText()
                        Log.d("HTML Content: ", name)
                    }
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }

            initialized = true
        }*/

    }
}

fun copyFileToDestination(context: Context, uri: Uri, destination: String) {
    try {
        val contentResolver = context.contentResolver
        // Get the InputStream from the URI
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IOException("Failed to open input stream from Uri")

        // Create a destination file
        val destinationFile = File(context.filesDir, destination)
        destinationFile.parentFile?.mkdirs()
        destinationFile.createNewFile()

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

@Composable
internal fun BoxScope.FilePickerScreen(onBookPicked: (Uri) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onBookPicked(it) }
    }

    ExtendedFloatingActionButton(
        text = {},
        icon = { Icon(Icons.Filled.Add, contentDescription = "") },
        onClick = {
            launcher.launch("*/*")
        },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    )
}

internal fun getLibrary(context: Context): Map<String, List<Book>> {
    val books = RecordKeeper.getInstance(context).getBookList()
    if (books.isEmpty()) return mutableMapOf()

    val library = mutableMapOf<String, MutableList<Book>>()

    for (book in books) {
        library.getOrPut(book.author) { mutableListOf<Book>() }.add(book)
    }

    return library
}