package com.devilishtruthstare.scribereader.jmdict

data class Sense (
    var pos: List<String>,
    var xref: List<String>,
    var field: List<String>,
    var gloss: List<String>,
    var misc: List<String>
)