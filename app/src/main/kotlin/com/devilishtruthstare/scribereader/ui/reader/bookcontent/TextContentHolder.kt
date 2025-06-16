package com.devilishtruthstare.scribereader.ui.reader.bookcontent

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
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
        ttsButton.visibility = if (section.isActive) View.VISIBLE else View.GONE
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
