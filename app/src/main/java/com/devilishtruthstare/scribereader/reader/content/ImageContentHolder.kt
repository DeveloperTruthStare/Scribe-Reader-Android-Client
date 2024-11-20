package com.devilishtruthstare.scribereader.reader.content

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content

class ImageContentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.item_image)

    fun bind(section: Content) {
        Glide.with(itemView.context)
            .load(section.imageFile)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle the load failure here (e.g., log the error or show a placeholder)
                    Log.e("Glide", "Image load failed", e)
                    return false // Return false to let Glide handle the error placeholder if any
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Code here will run once the image is successfully loaded
                    Log.d("Glide", "Image loaded successfully")
                    return false // Return false to let Glide set the image on
                }
            })
            .into(imageView)
    }
}