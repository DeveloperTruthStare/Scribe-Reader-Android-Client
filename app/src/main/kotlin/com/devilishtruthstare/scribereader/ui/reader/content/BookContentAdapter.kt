package com.devilishtruthstare.scribereader.ui.reader.content

import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.ui.reader.Reader
import com.devilishtruthstare.scribereader.ui.reader.bookcontent.PictureContentHolder
import com.devilishtruthstare.scribereader.ui.reader.bookcontent.TextContentHolder
import com.devilishtruthstare.scribereader.ui.reader.learningmodule.LearningModuleContentHolder

class BookContentAdapter(
    private val book: Book,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_PREVIOUS_CHAPTER = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_IMAGE = 2
        private const val TYPE_TEXT = 3
        private const val TYPE_SPACE = 4
        private const val TYPE_LEARNING_MODULE = 5
    }

    override fun getItemViewType(position: Int): Int {
        return if (!book.isParsed) {
            TYPE_LOADING
        } else if (position == 0) {
            TYPE_PREVIOUS_CHAPTER
        } else if ((position-1 == book.currentSection+1 && !showingLearningModule) ||
                   (position-1 == book.currentSection + 2 && showingLearningModule)) {
            TYPE_SPACE
        } else if (position-1 == book.currentSection + 1 && showingLearningModule) {
            TYPE_LEARNING_MODULE
        } else if (book.getParagraph(position-1).isImage) {
            TYPE_IMAGE
        } else {
            TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
                TextContentHolder(view) { content ->
                    book.playTTS(content)
                }
            }
            TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
                PictureContentHolder(view)
            }
            TYPE_SPACE -> {
                val spacer = Space(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        Resources.getSystem().displayMetrics.heightPixels
                    )
                }
                SpaceContentHolder(spacer)
            }
            TYPE_LOADING -> {
                val textView = TextView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                textView.text = "Loading Book"
                textView.gravity = Gravity.CENTER
                SpaceContentHolder(textView)
            }
            TYPE_LEARNING_MODULE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.learning_module_view, parent, false)
                LearningModuleContentHolder(view)
            }
            TYPE_PREVIOUS_CHAPTER -> {
                val textView = TextView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    textAlignment = View.TEXT_ALIGNMENT_INHERIT
                }
                textView.text = "Previous Chapter"
                SpaceContentHolder(textView)
            }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextContentHolder -> holder.bind(book.getParagraph(position-1))
            is PictureContentHolder -> holder.bind(book.getParagraph(position-1))
            is LearningModuleContentHolder -> holder.bind(book.nextSectionWords)
        }
    }

    var showingLearningModule = false

    override fun getItemCount(): Int {
        if (!book.isParsed) return 1
        // Previous Chapter, Ending Space, currentSection (0 index) potential learning module
        return 1 + 1 + book.currentSection + 1 + if (showingLearningModule) 1 else 0
    }
}
