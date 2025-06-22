package com.devilishtruthstare.scribereader.jmdict.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.jmdict.DictionaryUtils
import com.devilishtruthstare.scribereader.jmdict.data.Sense


internal class SenseView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, index: Int = 1, sense: Sense? = null) : FrameLayout(context, attrs) {
    private val posText: TextView
    init {
        LayoutInflater.from(context).inflate(R.layout.dictionary_sense_view, this, true)
        posText = findViewById(R.id.posText)

        for (i in sense!!.pos.indices) {
            sense.pos[i] = DictionaryUtils.convertPOS(sense.pos[i])
        }
        posText.text = sense.pos.toString()

        findViewById<TextView>(R.id.glossText).text = resources.getString(R.string.gloss_text, index+1, sense.gloss.toString())
    }
}
