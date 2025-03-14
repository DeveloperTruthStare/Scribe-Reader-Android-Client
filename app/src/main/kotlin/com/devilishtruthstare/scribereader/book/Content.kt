package com.devilishtruthstare.scribereader.book

import nl.siegmann.epublib.domain.Resource

data class Content (
    var isImage: Boolean,
    var content: String,
    var tokens: List<Token>,

    var imageResource: Resource?,
    var onPlaySoundClick: () -> Unit
)
