package com.devilishtruthstare.scribereader.jmdict

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream
import java.io.InputStreamReader


class JMDictParser {
    fun parseJson(inputStream: InputStream): List<Entry> {
        val gson = Gson()
        val reader = InputStreamReader(inputStream)

        // Specify the type of the List<Entry> for Gson to use
        val entryListType = object : TypeToken<List<Entry>>() {}.type

        // Parse the JSON and return the list of entries
        return gson.fromJson<List<Entry>>(reader, entryListType).also {
            reader.close()
        }
    }
}