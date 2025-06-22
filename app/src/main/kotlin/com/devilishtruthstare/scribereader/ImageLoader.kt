package com.devilishtruthstare.scribereader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class ImageLoader (private val context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: ImageLoader? = null
        fun getInstance(context: Context): ImageLoader {
            return INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: ImageLoader(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    private val imageMap: MutableMap<String, Bitmap> = mutableMapOf()

    fun getCoverImage(title: String): Bitmap {
        if (imageMap.containsKey(title)) {
            return imageMap[title]!!
        }

        val file = File(context.filesDir, "books/$title/$title.epub")
        FileInputStream(file).use { inputStream ->
            val epubReader = EpubReader().readEpub(inputStream)
            val coverImage = findCoverResource(epubReader)
            val bitmap = BitmapFactory.decodeStream(coverImage.inputStream)
            imageMap[title] = bitmap
            return bitmap
        }
    }

    private fun findCoverResource(book: nl.siegmann.epublib.domain.Book) : Resource {
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