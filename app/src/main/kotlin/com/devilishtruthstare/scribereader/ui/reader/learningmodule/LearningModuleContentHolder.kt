package com.devilishtruthstare.scribereader.ui.reader.learningmodule

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.google.android.flexbox.FlexboxLayout

class LearningModuleContentHolder (
    private val itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val wordContainer: FlexboxLayout = itemView.findViewById(R.id.learning_word_container)

    fun bind(unseenWords: MutableList<String>) {
        wordContainer.removeAllViews()
        for (word in unseenWords) {
            val entries = JMDict.getInstance(itemView.context).getEntries(word)
            var know = true
            for (entry in entries) {
                if (entry.level == 0) {
                    know = false
                    break
                }
            }
            val wordButton = WordButton(
                itemView.context,
                displayText = word,
                isKnown = know
            )
            wordButton.setOnClickListener {
                JMDict.getInstance(itemView.context).markAsLearned(word)
                wordButton.setKnown()
            }
            wordContainer.addView(wordButton)
        }
    }
}