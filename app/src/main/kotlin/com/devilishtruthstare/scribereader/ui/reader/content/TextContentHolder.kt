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

    fun bind(section: Content) {
        textContainer.removeAllViews()
        for(token in section.tokens) {
            textContainer.addView(TokenView(itemView.context, token = token))
        }
        ttsButton.setOnClickListener {
            section.onPlaySoundClick()
        }
    }
}
