package com.devilishtruthstare.scribereader.editor.actions

import android.content.Context
import android.net.Uri
import android.util.Log
import com.devilishtruthstare.scribereader.ui.tabview.TabItem

class FileInputAction(
    context: Context,
    private val title: String,
    uri: Uri,
    private val setText: (text: String) -> Unit
) : TabItem, Action {
    private val fileContents: String
    init {
        val inputStream = context.contentResolver.openInputStream(uri)
        fileContents = inputStream?.bufferedReader()?.use { it.readText() }!!
        Log.d("FileInputAction", fileContents)
    }
    override fun getTitle(): String {
        return title
    }

    override fun onSelected(){
        setText(getOutput())
    }

    override fun getOutput(): String {
        return fileContents
    }

    override fun addInput(step: Action) {
        // This does nothing
    }

}
