package com.devilishtruthstare.scribereader.jmdict.data

data class Sense (
    var pos: MutableList<String>,
    var xref: List<String>,
    var field: List<String>,
    var gloss: List<String>,
    var misc: List<String>
)