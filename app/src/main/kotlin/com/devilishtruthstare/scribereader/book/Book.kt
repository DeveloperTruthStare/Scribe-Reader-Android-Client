package com.devilishtruthstare.scribereader.book

import android.content.Context
import com.devilishtruthstare.scribereader.jmdict.Dictionary
import com.devilishtruthstare.scribereader.ui.reader.Reader

class Book {
    companion object {
        const val VERTICAL_TEXT_DEFAULT = "DEFAULT"
        const val VERTICAL_TEXT_TRUE = "FORCED_ON"
        const val VERTICAL_TEXT_FALSE = "FORCED_OFF"

        const val NEXT_CHAPTER = "NEXT_CHAPTER"
        const val END_OF_BOOK = "BOOK_FINISHED"
        const val NEXT_PARAGRAPH = "NEXT_PARAGRAPH"
    }
    var bookId: Int = -1
    var coverImage: String = ""
    var title: String = ""
    var author: String = ""
    var language: String = ""

    var fileLocation: String = ""
    var chapters: MutableList<Chapter> = mutableListOf()

    var status: String = "NOT_STARTED"

    var currentChapter: Int = 0
    var currentSection: Int = 0
    var textMode: String = "DEFAULT"

    private var activeReader: Reader? = null
    fun setActiveReader(reader: Reader) {
        activeReader = reader
    }

    var isParsed = false

    fun openBook(context: Context) {
        if (status == RecordKeeper.STATUS_NOT_STARTED) {
            RecordKeeper.getInstance(context).startBook(bookId)
        }
        RecordKeeper.getInstance(context).addOpenHistory(bookId)
        isParsed = true
        chapters[currentChapter].content[currentSection].isActive = true
    }

    fun getParagraph(paragraph: Int): Content {
        return chapters[currentChapter].content[paragraph]
    }

    fun next(): String {
        var result = NEXT_PARAGRAPH
        chapters[currentChapter].content[currentSection].isActive = false
        currentSection++
        if (currentSection >= chapters[currentChapter].content.size) {
            result = nextChapter()
        }
        chapters[currentChapter].content[currentSection].isActive = true

        return result
    }

    var nextSectionWords: MutableList<String> = mutableListOf()
    fun prepareNextSection(context: Context): Boolean {
        nextSectionWords = mutableListOf()

        var nextSection = currentSection + 1
        var nextChapter = currentChapter
        if (nextSection >= chapters[nextChapter].content.size) {
            nextChapter++
            nextSection = 0
            if (nextChapter >= chapters.size) {
                return false
            }
        }
        val section = chapters[nextChapter].content[nextSection]
        if (section.isImage) return false

        for (token in section.tokens) {
            val searchTerm = Token.getSearchTerm(token)
            if (token.features.isEmpty() ||
                token.features[0] in Token.IGNORED_MARKERS || searchTerm in nextSectionWords) continue

            val entries = Dictionary.getInstance(context).search(searchTerm)
            for (entry in entries) {
                if (entry.level == 0) {
                    nextSectionWords.add(searchTerm)
                    break
                }
            }
        }
        return nextSectionWords.size > 0
    }

    fun getCurrentSection(): Content {
        return chapters[currentChapter].content[currentSection]
    }
    private fun nextChapter(): String {
        currentSection = 0
        currentChapter++
        if (currentChapter >= chapters.size) {
            return END_OF_BOOK
        }
        return NEXT_CHAPTER
    }
    fun playTTS(content: String) {
        activeReader?.playSound(content)
    }
}
