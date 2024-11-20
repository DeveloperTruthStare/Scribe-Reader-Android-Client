package com.devilishtruthstare.scribereader.library

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Library
import com.devilishtruthstare.scribereader.database.RecordKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class DownloadedBooksTracker(
    private val context: Context,
    private val recordKeeper: RecordKeeper
) {
    companion object {
        const val BASE_URI = "http://10.0.2.2/api"
        //const val BASE_URI = "http://172.20.8.76/api"
        //const val BASE_URI = "http://192.168.137.1/api"
    }
    fun downloadBookList(onFailure: ((Call, IOException) -> Unit), onSuccess: ((Library) -> Unit)) {

        val client = OkHttpClient()

        val request = Request.Builder().url("${BASE_URI}/getbooklist").build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure(call, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonString = response.body?.string()

                    val gson = Gson()
                    val bookList = gson.fromJson(jsonString, Library::class.java)
                    onSuccess(bookList)
                }
            }
        })
    }
    fun downloadBook(book: Book, onProgressUpdate: (progress: Int) -> Unit, onDownloadComplete: () -> Unit) {
        if (recordKeeper.hasBook(book.title)) {
            Log.e("DOWNLOAD_BOOK", "${book.title} is already downloaded")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            val outputDir = File(context.filesDir, "books")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            val url = "$BASE_URI/getbook?title=${book.title}"
            var connection: HttpURLConnection? = null

            try {
                val zipFile = File(outputDir, "downloaded.zip")
                connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                val fileLength = connection.contentLength
                if (fileLength > 0) {
                    var totalBytesRead = 0
                    connection.inputStream.use { input ->
                        FileOutputStream(zipFile).use { output ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while(input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                totalBytesRead += bytesRead
                                val progress = (totalBytesRead.toDouble() / fileLength.toDouble()) * 100
                                withContext(Dispatchers.Main) {
                                    onProgressUpdate(progress.toInt())
                                }
                            }
                        }
                    }
                } else {
                    Log.e("DOWNLOAD_BOOK", "File Length was 0")
                }

                ZipInputStream(FileInputStream(zipFile)).use { zipInput ->
                    var entry = zipInput.nextEntry
                    while (entry != null) {
                        // Replace backslashes with forward slashes to fix path issues
                        val entryName = entry.name.replace("\\", "/")
                        val outFile = File(outputDir, entryName)

                        if (entry.isDirectory) {
                            outFile.mkdirs()
                        } else {
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { output ->
                                zipInput.copyTo(output)
                            }
                        }
                        entry = zipInput.nextEntry
                    }
                }

                recordKeeper.onBookDownloaded(book)
                withContext(Dispatchers.Main) {
                    onDownloadComplete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
            }
        }
    }
    fun deleteLocalBook(book: Book) {
        val file = File(context.filesDir, "books/${book.title}")
        file.delete()
        recordKeeper.onBookDeleted(book)
    }
}