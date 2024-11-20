package com.devilishtruthstare.scribereader.book

class Book {
    var title: String = ""
    var coverImage: String = ""
    var chapters: List<Chapter> = emptyList()

    var currentChapter: Int = 0
    var currentSection: Int = 0
}
