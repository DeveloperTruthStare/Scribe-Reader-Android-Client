package com.devilishtruthstare.scribereader.book

data class Token (
    var surface: String,
    var features: List<String>
) {
    companion object {
        fun getSearchTerm(token: Token): String = getFeatureOrSurface(token, 6)
        fun getFeatureOrSurface(token: Token, index: Int): String {
            return if (token.features.size > index && token.features[index] != "*") {
                token.features[index]
            } else {
                token.surface
            }
        }
        fun getFurigana(token: Token): String {
            return katakanaToHiragana(getFeatureOrSurface(token, 7))
        }

        fun katakanaToHiragana(input: String): String {
            return input.map { c ->
                if (c in 'ア'..'ン') (c - 0x60).toChar() else c
            }.joinToString("")
        }
    }
}