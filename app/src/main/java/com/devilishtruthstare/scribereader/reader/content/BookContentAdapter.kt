package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.database.FlashCard


class BookContentAdapter(
    private val contentList: MutableList<Content>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_IMAGE = 0
        private const val TYPE_TEXT = 1
        private const val TYPE_SPACE = 2
    }
    private val flashCard: FlashCard = FlashCard(context)

    override fun getItemViewType(position: Int): Int {
        return if (position == contentList.size) {
            TYPE_SPACE
        } else if (contentList[position].isImage) {
            TYPE_IMAGE
        } else {
            TYPE_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_text, parent, false)
                TextContentHolder(view, context, flashCard)
            }
            TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
                ImageContentHolder(view)
            }
            TYPE_SPACE -> {
                val spacer = Space(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        Resources.getSystem().displayMetrics.heightPixels / 4
                    )
                }
                SpaceContentHolder(spacer)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextContentHolder -> holder.bind(contentList[position])
            is ImageContentHolder -> holder.bind(contentList[position])
        }
    }

    override fun getItemCount(): Int = contentList.size + 1
}
