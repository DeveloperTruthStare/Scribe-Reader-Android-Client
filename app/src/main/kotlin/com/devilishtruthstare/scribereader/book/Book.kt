package com.devilishtruthstare.scribereader.book

class Book {
    var bookId: Int = -1
    var coverImage: String = ""
    var title: String = ""
    var author: String = ""
    var language: String = ""

    var fileLocation: String = ""
    var chapters: MutableList<Chapter> = mutableListOf()

    var currentChapter: Int = 0
    var currentSection: Int = 0
}
