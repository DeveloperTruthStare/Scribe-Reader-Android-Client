package com.devilishtruthstare.scribereader.book

import com.github.wanasit.kotori.Token
import com.github.wanasit.kotori.optimized.DefaultTermFeatures
import java.io.File

data class Content (
    var isImage: Boolean,
    var imageUrl: String,
    var text: String,
    var tokens: List<Token<DefaultTermFeatures>>,

    var imageFile: File,
    var onPlaySoundClick: () -> Unit
)