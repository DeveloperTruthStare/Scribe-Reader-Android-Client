package com.devilishtruthstare.scribereader.ui.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.devilishtruthstare.scribereader.ImageLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CoverImage(title: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmapState by produceState<Bitmap?>(initialValue = null, title) {
        value = withContext(Dispatchers.IO) {
            try {
                ImageLoader.getInstance(context).getCoverImage(title)
            } catch (e: Exception) {
                null
            }
        }
    }

    bitmapState?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Cover of $title",
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
        )
    }
}