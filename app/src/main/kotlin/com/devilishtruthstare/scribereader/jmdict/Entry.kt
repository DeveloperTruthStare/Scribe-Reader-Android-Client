package com.devilishtruthstare.scribereader.jmdict

data class Entry (
    var entSeq: Int,
    var kanji: List<String>,
    var kana: List<String>,
    var senses: List<Sense>
)