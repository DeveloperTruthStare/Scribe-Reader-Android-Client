package com.devilishtruthstare.scribereader.book.utils

import android.content.Context
import android.util.Log
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Chapter
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.dictionary.DictionaryUtils.Companion.jsonToTokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Stack
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class BookParser(private val context: Context) {
    companion object {
        fun parseBook(context: Context, book: Book): Deferred<Book>{
            return CoroutineScope(Dispatchers.Default).async {
                BookParser(context).parseBook(book)
            }
        }
        fun finishParsing(context: Context, bookFromTokens: Book, bookRecord: Book): Book {
            bookFromTokens.title = bookRecord.title
            bookFromTokens.coverImage = bookRecord.coverImage
            bookFromTokens.bookId = bookRecord.bookId
            bookFromTokens.status = bookRecord.status
            bookFromTokens.author = bookRecord.author
            bookFromTokens.currentChapter = bookRecord.currentChapter
            bookFromTokens.currentSection = bookRecord.currentSection
            bookFromTokens.fileLocation = bookRecord.fileLocation
            bookFromTokens.language = bookRecord.language
            bookFromTokens.verticalTextPreference = bookRecord.verticalTextPreference
            return BookParser(context).finishParsing(bookFromTokens)
        }
    }
    private val t: tokenizer.Tokenizer_ = tokenizer.Tokenizer.newTokenizer()
    private val imageMap: MutableMap<String, Resource> = mutableMapOf()
    fun finishParsing(bookDto: Book): Book {
        val inputStream: InputStream = FileInputStream(File(context.filesDir, "books/${bookDto.title}/${bookDto.title}.epub"))
        val book = EpubReader().readEpub(inputStream)

        val imageExtensions = setOf(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp")
        for((_, resource) in book.resources.resourceMap) {
            if (resource.mediaType.defaultExtension in imageExtensions) {
                imageMap[getFileNameWithoutExtension(resource.href)] = resource
            }
        }

        for((chapter_index, chapter) in bookDto.chapters.withIndex()) {
            for ((paragraph_index, paragraph) in chapter.content.withIndex()) {
                if (paragraph.isImage) {
                    bookDto.chapters[chapter_index].content[paragraph_index].imageResource = imageMap[getFileNameWithoutExtension(paragraph.content)]
                }
            }
        }

        return bookDto
    }
    suspend fun parseBook(bookDto: Book): Book = withContext(Dispatchers.Default){
        val inputStream: InputStream = FileInputStream(File(context.filesDir, "books/${bookDto.title}/${bookDto.title}.epub"))
        val book = EpubReader().readEpub(inputStream)
        val toc = book.tableOfContents
        toc.tocReferences.forEach { reference ->

        }

        val imageExtensions = setOf(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp")
        for((_, resource) in book.resources.resourceMap) {
            if (resource.mediaType.defaultExtension in imageExtensions) {
                imageMap[getFileNameWithoutExtension(resource.href)] = resource
            }
        }

        val resources = book.spine.spineReferences.map { it.resource }
        val chapters = resources.map { resource ->
            async { return@async parseFile(resource.reader.readText().trim()) }
        }
        bookDto.chapters = chapters.awaitAll().toMutableList()
        bookDto.chapters.removeIf { it.content.isEmpty() }

        return@withContext bookDto
    }

    fun parseFile(rawText: String): Chapter {
        val document = Jsoup.parse(rawText)
        return Chapter().apply {
            title = document.title()
            content = traverseNodesSequence(document.body()).toMutableList()
        }
    }

    private fun traverseNodesSequence(element: Element): Sequence<Content> = sequence {
        when (element.tagName()) {
            "p" -> {
                parsePTag(element)?.let { yield(it) }
                return@sequence
            }
            "img", "image" -> {
                val src = element.attr("src").ifBlank { element.attr("href") }
                if (src.isNotBlank()) yield(createImageSection(src))
                return@sequence
            }
            else -> {
                for(child in element.children()) {
                    yieldAll(traverseNodesSequence(child))
                }
            }
        }
    }

    private fun parsePTag(root: Element): Content? {
        val result = StringBuilder()
        var imageContent: Content? = null

        val stack: Stack<Node> = Stack()
        stack.push(root)

        while (stack.isNotEmpty()) {
            val node = stack.pop()

            when (node) {
                is TextNode -> {
                    result.append(node.text())
                }
                is Element -> {
                    when (node.tagName()) {
                        "rt" -> {
                            // ignore it
                        }
                        "img", "image" -> {
                            val src = node.attr("src").ifBlank { node.attr("href") }
                            if (src.isNotBlank()) imageContent = createImageSection(src)
                        }
                        else -> {
                            for (n in node.childNodes().reversed()) {
                                stack.push(n)
                            }
                        }
                    }
                }
                else -> {
                    Log.d("BookParser", "Could not Parse node: $node")
                }
            }
        }

        return imageContent ?: createTextSection(result.toString().trim())
    }

    private fun createTextSection(text: String): Content? {
        if (text.isEmpty()) return null
        val tokensJson = t.tokenize(text)
        val tokens = jsonToTokens(tokensJson)
        return Content(false, text, tokens, false, null) { }
    }

    private fun createImageSection(src: String): Content =
        Content(true, src, mutableListOf(), false, imageMap[getFileNameWithoutExtension(src)]!!) { }

    private fun getFileNameWithoutExtension(filePath: String): String = File(filePath).nameWithoutExtension

}
