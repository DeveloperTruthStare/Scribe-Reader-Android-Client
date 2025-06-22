package com.devilishtruthstare.scribereader.book

import nl.siegmann.epublib.domain.Resource

data class Content (
    var isImage: Boolean,
    var content: String,
    var tokens: MutableList<Token>,
    var isActive: Boolean,

    var imageResource: Resource?,
)
