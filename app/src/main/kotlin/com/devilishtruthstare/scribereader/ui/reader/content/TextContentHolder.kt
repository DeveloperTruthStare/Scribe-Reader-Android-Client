package com.devilishtruthstare.scribereader.ui.reader.content

import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.dictionary.ui.DictionaryView
import com.google.android.flexbox.FlexboxLayout

class TextContentHolder(
    private val itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val textContainer: FlexboxLayout = itemView.findViewById(R.id.container)
    private val ttsButton: ImageView = itemView.findViewById(R.id.tts_button)
    private val textViews: MutableList<TokenView> = mutableListOf()

    fun bind(section: Content) {
        textContainer.removeAllViews()
        textViews.clear()

        for(token in section.tokens) {
            val textView = TokenView(itemView.context, token = token, onUpdated = {
                refreshTextViews()
            }, isActive = section.isActive)
            textViews.add(textView)
            textContainer.addView(textView)
        }
        ttsButton.setOnClickListener {
            section.onPlaySoundClick()
        }
    }

    private fun refreshTextViews() {
        for (view in textViews) {
            view.setupView()
        }
    }
}
