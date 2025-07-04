package com.devilishtruthstare.scribereader.editor.actions

import android.widget.EditText
import com.devilishtruthstare.scribereader.ui.tabview.TabItem

class JsonPrettyAction(
    private val title: String,
    private val input: Action,
    private val setText: (text: String) -> Unit
) : TabItem, Action {
    override fun getTitle(): String {
        return title
    }

    override fun onSelected() {
        setText(getOutput())
    }

    override fun addInput(step: Action) {
        // Do Nothing
    }

    override fun getOutput(): String {
        // Make Pretty
        val ss = StringBuilder()
        var indent = 0
        val tabString = "    "
        for (char in input.getOutput()) {
            when (char) {
                '{', '[' -> {
                    ss.append(char)
                    ss.append('\n')
                    indent++
                    ss.append(tabString.repeat(indent))
                }
                '}', ']' -> {
                    ss.append('\n')
                    indent--
                    ss.append(tabString.repeat(indent))
                    ss.append(char)
                }
                ',' -> {
                    ss.append(char)
                    ss.append('\n')
                    ss.append(tabString.repeat(indent))
                }
                else -> ss.append(char)
            }
        }

        return ss.toString()
    }
}