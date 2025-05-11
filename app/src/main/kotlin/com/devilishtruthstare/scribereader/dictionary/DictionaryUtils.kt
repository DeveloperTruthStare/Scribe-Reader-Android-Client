package com.devilishtruthstare.scribereader.dictionary

import com.devilishtruthstare.scribereader.book.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader

class DictionaryUtils {
    companion object {
        private val posDictionary: Map<String, String> = mapOf(
            "adj-f" to "Noun or verb acting prenominally",
            "adj-i" to "Adjective (keiyoushi)",
            "adj-ix" to "Adjective (keiyoushi) - yoi/ii class",
            "adj-kari" to "'Kari' adjective (archaic)",
            "adj-ku" to "'Ku' adjective (archaic)",
            "adj-na" to "Adjectival nouns or quasi-adjectives (keiyoudoushi)",
            "adj-nari" to "Archaic/formal form of na-adjective",
            "adj-no" to "Nouns which may take the genitive case particle 'no'",
            "adj-pn" to "Pre-noun adjectival (rentaishi)",
            "adj-shiku" to "'Shiku' adjective (archaic)",
            "adj-t" to "'Taru' adjective",
            "adv" to "Adverb (fukushi)",
            "adv-to" to "Adverb taking the 'to' particle",
            "aux" to "Auxiliary",
            "aux-adj" to "Auxiliary adjective",
            "aux-v" to "Auxiliary verb",
            "conj" to "Conjunction",
            "cop" to "Copula",
            "ctr" to "Counter",
            "exp" to "Expressions (phrases, clauses, etc.)",
            "int" to "Interjection (kandoushi)",
            "n" to "Noun (common) (futsuumeishi)",
            "n-adv" to "Adverbial noun (fukushitekimeishi)",
            "n-pr" to "Proper noun",
            "n-pref" to "Noun, used as a prefix",
            "n-suf" to "Noun, used as a suffix",
            "n-t" to "Noun (temporal) (jisoumeishi)",
            "num" to "Numeric",
            "pn" to "Pronoun",
            "pref" to "Prefix",
            "prt" to "Particle",
            "suf" to "Suffix",
            "unc" to "Unclassified",
            "v-unspec" to "Verb unspecified",
            "v1" to "Ichidan verb",
            "v1-s" to "Ichidan verb - kureru special class",
            "v2a-s" to "Nidan verb with 'u' ending (archaic)",
            "v2b-k" to "Nidan verb (upper class) with 'bu' ending (archaic)",
            "v2b-s" to "Nidan verb (lower class) with 'bu' ending (archaic)",
            "v2d-k" to "Nidan verb (upper class) with 'dzu' ending (archaic)",
            "v2d-s" to "Nidan verb (lower class) with 'dzu' ending (archaic)",
            "v2g-k" to "Nidan verb (upper class) with 'gu' ending (archaic)",
            "v2g-s" to "Nidan verb (lower class) with 'gu' ending (archaic)",
            "v2h-k" to "Nidan verb (upper class) with 'hu/fu' ending (archaic)",
            "v2h-s" to "Nidan verb (lower class) with 'hu/fu' ending (archaic)",
            "v2k-k" to "Nidan verb (upper class) with 'ku' ending (archaic)",
            "v2k-s" to "Nidan verb (lower class) with 'ku' ending (archaic)",
            "v2m-k" to "Nidan verb (upper class) with 'mu' ending (archaic)",
            "v2m-s" to "Nidan verb (lower class) with 'mu' ending (archaic)",
            "v2n-s" to "Nidan verb (lower class) with 'nu' ending (archaic)",
            "v2r-k" to "Nidan verb (upper class) with 'ru' ending (archaic)",
            "v2r-s" to "Nidan verb (lower class) with 'ru' ending (archaic)",
            "v2s-s" to "Nidan verb (lower class) with 'su' ending (archaic)",
            "v2t-k" to "Nidan verb (upper class) with 'tsu' ending (archaic)",
            "v2t-s" to "Nidan verb (lower class) with 'tsu' ending (archaic)",
            "v2w-s" to "Nidan verb (lower class) with 'u' ending and 'we' conjugation (archaic)",
            "v2y-k" to "Nidan verb (upper class) with 'yu' ending (archaic)",
            "v2y-s" to "Nidan verb (lower class) with 'yu' ending (archaic)",
            "v2z-s" to "Nidan verb (lower class) with 'zu' ending (archaic)",
            "v4b" to "Yodan verb with 'bu' ending (archaic)",
            "v4g" to "Yodan verb with 'gu' ending (archaic)",
            "v4h" to "Yodan verb with 'hu/fu' ending (archaic)",
            "v4k" to "Yodan verb with 'ku' ending (archaic)",
            "v4m" to "Yodan verb with 'mu' ending (archaic)",
            "v4n" to "Yodan verb with 'nu' ending (archaic)",
            "v4r" to "Yodan verb with 'ru' ending (archaic)",
            "v4s" to "Yodan verb with 'su' ending (archaic)",
            "v4t" to "Yodan verb with 'tsu' ending (archaic)",
            "v5aru" to "Godan verb - -aru special class",
            "v5b" to "Godan verb with 'bu' ending",
            "v5g" to "Godan verb with 'gu' ending",
            "v5k" to "Godan verb with 'ku' ending",
            "v5k-s" to "Godan verb - Iku/Yuku special class",
            "v5m" to "Godan verb with 'mu' ending",
            "v5n" to "Godan verb with 'nu' ending",
            "v5r" to "Godan verb with 'ru' ending",
            "v5r-i" to "Godan verb with 'ru' ending (irregular verb)",
            "v5s" to "Godan verb with 'su' ending",
            "v5t" to "Godan verb with 'tsu' ending",
            "v5u" to "Godan verb with 'u' ending",
            "v5u-s" to "Godan verb with 'u' ending (special class)",
            "v5uru" to "Godan verb - Uru old class verb (old form of Eru)",
            "vi" to "Intransitive verb",
            "vk" to "Kuru verb - special class",
            "vn" to "Irregular nu verb",
            "vr" to "Irregular ru verb, plain form ends with -ri",
            "vs" to "Noun or participle which takes the aux. verb suru",
            "vs-c" to "Su verb - precursor to the modern suru",
            "vs-i" to "Suru verb - included",
            "vs-s" to "Suru verb - special class",
            "vt" to "Transitive verb",
            "vz" to "Ichidan verb - zuru verb (alternative form of -jiru verbs)"
        )
        fun convertPOS(jmDictPOS: String): String {
            return posDictionary[jmDictPOS] ?: jmDictPOS
        }
        fun parseJson(inputStream: InputStream): List<Entry> {
            val gson = Gson()
            val reader = InputStreamReader(inputStream)

            val entryListType = object : TypeToken<List<Entry>>() {}.type

            return gson.fromJson<List<Entry>>(reader, entryListType).also {
                reader.close()
            }
        }
        fun jsonToTokens(jsonString: String): MutableList<Token> {
            val gson = Gson()
            val type = object : TypeToken<List<Token>>() {}.type
            return gson.fromJson(jsonString, type)
        }
    }
}