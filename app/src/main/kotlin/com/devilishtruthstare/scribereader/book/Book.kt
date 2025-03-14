package com.devilishtruthstare.scribereader.book

class Book {
    var title: String = ""
    var coverImage: String = ""
    var fileLocation: String = ""
    var chapters: MutableList<Chapter> = mutableListOf()

    var currentChapter: Int = 0
    var currentSection: Int = 0
}
