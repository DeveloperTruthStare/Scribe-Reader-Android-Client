package com.devilishtruthstare.scribereader.reader

import android.util.Log
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Chapter
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.book.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.siegmann.epublib.domain.Resource
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import tokenizer.Tokenizer_
import java.io.File

class BookParser(private val book: nl.siegmann.epublib.domain.Book, private val tokenizer: Tokenizer_) {
    private val imageMap: MutableMap<String, Resource> = mutableMapOf()
    private val bookDto: Book = Book()
    private var currentChapter: Int = 0
    private var currentSection: Int = 0

    init {
        bookDto.title = book.title

        val imageExtensions = setOf(".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp")

        for((_, resource) in book.resources.resourceMap) {
            if (resource.mediaType.defaultExtension in imageExtensions) {
                imageMap[getFileNameWithoutExtension(resource.href)] = resource
            }
        }
    }

    fun setChapter(chapter: Int) {
        if (chapter < bookDto.chapters.size) {
            currentChapter = chapter
        }
    }
    fun setSection(section: Int) {
        if (section < bookDto.chapters[currentChapter].content.size) {
            currentSection = section
        }
    }
    fun getTitle(): String {
        return book.title
    }
    fun getCurrentChapter(): Int { return currentChapter }
    fun getCurrentSection(): Int { return currentSection }
    fun getChapterSize(): Int { return bookDto.chapters[currentChapter].content.size }
    fun endOfChapter(): Boolean { return currentSection-1 == bookDto.chapters[currentChapter].content.size}
    fun endOfBook(): Boolean { return currentChapter == bookDto.chapters.size-1 }

    fun nextChapter() {
        currentChapter++
        currentSection = 0
    }
    fun nextSection(): Content {
        return bookDto.chapters[currentChapter].content[currentSection++]
    }

    fun processBook() {
        // Look ahead through the entire book
        val resources = book.spine.spineReferences.map { it.resource }
        if (book.tableOfContents == null) {
            Log.e("TOC", "No TOC FOUND")
        } else {
            for (reference in book.tableOfContents.tocReferences) {
                Log.d("TABLEOFCONTENTS", reference.completeHref)
            }
        }

        bookDto.chapters.add(Chapter())

        for (resource in resources) {
            val text = resource.reader.readText().trim()
            val body = Jsoup.parse(text).body()
            var skip = false
            for (className in body.classNames()) {
                if (className == "p-caution" || className == "p-colophon") {
                    skip = true
                    break
                }
            }
            if (skip) {
                continue
            }

            var newSections = mutableListOf<Content>()
            traverseNodes(body, newSections)

            var containsText = false

            for (section in newSections) {
                bookDto.chapters[bookDto.chapters.size-1].content.add(section)

                if (!section.isImage) {
                    containsText = true
                }
            }

            if (containsText) {
                //bookDto.chapters.add(Chapter())
            }
        }

        // Trim last Chapter if it is empty
        if (bookDto.chapters[bookDto.chapters.size-1].content.isEmpty()) {
            bookDto.chapters.removeAt(bookDto.chapters.size-1)
        }
    }
    private fun traverseNodes(element: Element, chapter: MutableList<Content>) {
        if (element.tagName() == "p") {
            val result = StringBuilder()

            // Iterate over all child nodes of the <p> tag
            for (node in element.childNodes()) {
                when {
                    node.nodeName() == "ruby" -> {
                        val rubyElement = node as Element
                        result.append(rubyElement.ownText())
                    }
                    node.nodeName() == "#text" -> {
                        result.append(node.toString())
                    }
                    node.nodeName() == "img" || node.nodeName() == "image" -> {
                        val imgElement = node as Element
                        val src = imgElement.attr("src")
                        val href = imgElement.attr("href")
                        if (src.isNotBlank()) {
                            chapter.add(createImageSection(src))
                        } else if (href.isNotBlank()) {
                            chapter.add(createImageSection(href))
                        }
                    }
                }
            }
            if (result.trim() != "") {
                chapter.add(createTextSection(result.toString().trim()))
            }
        }

        for (child in element.children()) {
            traverseNodes(child, chapter)
        }
    }

    private fun jsonToTokens(jsonString: String): List<Token> {
        val gson = Gson()
        val type = object : TypeToken<List<Token>>() {}.type
        return gson.fromJson(jsonString, type)
    }
    private fun createTextSection(text: String): Content {
        val tokensJson = tokenizer.tokenize(text)
        val tokens = jsonToTokens(tokensJson)

        return Content(
            isImage = false,
            content = text,
            tokens = tokens,
            imageResource = null,
            onPlaySoundClick = { }
        )
    }
    private fun createImageSection(src: String): Content {
        return Content(
            isImage = true,
            content = src,
            tokens = emptyList(),
            imageResource = imageMap[getFileNameWithoutExtension(src)]!!,
            onPlaySoundClick = { }
        )
    }

    private fun getFileNameWithoutExtension(filePath: String): String {
        val file = File(filePath)  // Create a File object from the path
        return file.nameWithoutExtension  // Get the file name without the extension
    }
}