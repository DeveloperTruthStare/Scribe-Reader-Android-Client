package com.devilishtruthstare.scribereader.reader.content

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import java.io.InputStream

class ImageContentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.item_image)

    fun bind(section: Content) {
        section.imageResource?.let {
            val inputStream: InputStream = it.inputStream
            val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            imageView.setImageBitmap(bitmap)
        }
    }
}