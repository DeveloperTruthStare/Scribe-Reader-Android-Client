package com.devilishtruthstare.scribereader.book

data class Token (
    var surface: String,
    var features: List<String>
) {
    companion object {
        internal const val PUNCTUATION_MARKER = "記号"
        internal const val PARTICLE_MARKER = "助詞"
        internal const val CONJUGATION_MARKER = "助動詞"
        internal const val PRE_NOUN_ADJECTIVAL = "連体詞"
        internal const val NOUN_MARKER = "名詞"
        internal const val VERB_MARKER = "動詞"
        internal const val AUX_VERB_MARKER = "助動詞"
        internal const val CONJUNCTION_MARKER = "接続詞"
        internal const val I_ADJECTIVE_MARKER = "形容詞"
        internal const val ADVERB_MARKER = "副詞"
        internal const val PREFIX_MARKER = "接頭詞"
        internal const val INTERJECTION_MARKER = "感動詞"
        internal const val FILLER_MARKER = "フィラー"
        internal val IGNORED_MARKERS = listOf(PUNCTUATION_MARKER, FILLER_MARKER, PARTICLE_MARKER, AUX_VERB_MARKER)
        
        fun getSearchTerm(token: Token): String = getFeatureOrSurface(token, 6)
        fun getFeatureOrSurface(token: Token, index: Int, default: String = token.surface): String {
            return if (token.features.size > index && token.features[index] != "*") {
                token.features[index]
            } else {
                default
            }
        }
        fun getFurigana(token: Token): String {
            if (!containsKanji(token)) {
                return ""
            }
            return katakanaToHiragana(getFeatureOrSurface(token, 7, ""))
        }
        fun containsKanji(token: Token): Boolean {
            val kanjiRegex = Regex("[\\p{IsHan}]")
            return kanjiRegex.containsMatchIn(token.surface)
        }

        fun katakanaToHiragana(input: String): String {
            return input.map { c ->
                if (c in 'ア'..'ン') (c - 0x60).toChar() else c
            }.joinToString("")
        }
    }
}