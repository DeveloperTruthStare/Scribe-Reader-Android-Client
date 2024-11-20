package com.devilishtruthstare.scribereader.book

import com.devilishtruthstare.scribereader.Token
import java.io.File

data class Content (
    var isImage: Boolean,
    var imageUrl: String,
    var text: String,
    var tokens: List<Token>,

    var imageFile: File,
    var onPlaySoundClick: () -> Unit
)