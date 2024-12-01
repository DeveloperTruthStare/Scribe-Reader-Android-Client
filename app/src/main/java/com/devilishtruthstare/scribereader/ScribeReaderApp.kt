package com.devilishtruthstare.scribereader

import android.app.Application
import com.github.wanasit.kotori.Tokenizer
import com.github.wanasit.kotori.optimized.DefaultTermFeatures

class ScribeReaderApp : Application() {
    lateinit var tokenizer: Tokenizer<DefaultTermFeatures>
}