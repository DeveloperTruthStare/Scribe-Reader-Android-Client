package com.devilishtruthstare.scribereader.book

import com.devilishtruthstare.scribereader.database.RecordKeeper

class Book {
    companion object {
        const val VERTICAL_TEXT_DEFAULT = "DEFAULT"
        const val VERTICAL_TEXT_TRUE = "FORCED_ON"
        const val VERTICAL_TEXT_FALSE = "FORCED_OFF"

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
    var verticalTextPreference: String = VERTICAL_TEXT_DEFAULT
}
