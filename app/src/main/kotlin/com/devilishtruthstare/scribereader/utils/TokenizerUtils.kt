package com.devilishtruthstare.scribereader.utils

import com.devilishtruthstare.scribereader.book.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TokenizerUtils {
    companion object {
        @Volatile
        private var INSTANCE: TokenizerUtils? = null
        fun getInstance(): TokenizerUtils {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenizerUtils().also { INSTANCE = it }
            }
        }
    }
    private val t: tokenizer.Tokenizer_ = tokenizer.Tokenizer.newTokenizer()
    fun tokenize(input: String): MutableList<Token> {
        val tokensJson = t.tokenize(input)
        val gson = Gson()
        val type = object : TypeToken<List<Token>>() {}.type
        return gson.fromJson(tokensJson, type)
    }
}