package com.devilishtruthstare.scribereader.FileUtils

import android.content.Context
import java.io.File

internal const val ASSET_PREFIX = "assets://"

fun loadFile(context: Context, filePath: String): String {
    return if (filePath.substring(0, ASSET_PREFIX.length) == ASSET_PREFIX) {
        context.assets.open(filePath.substring(ASSET_PREFIX.length)).bufferedReader().use { it.readText() }
    } else {
        File(context.filesDir, "layouts/${filePath}").readText()
    }
}

fun saveStringToFile(context: Context, filename: String, content: String) {
    val filePath = filename.replace(ASSET_PREFIX, "")
    val file = File(context.filesDir, filename)
    file.parentFile?.mkdir()
    file.writeText(content)
}