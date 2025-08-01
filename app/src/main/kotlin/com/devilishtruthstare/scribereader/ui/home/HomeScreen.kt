package com.devilishtruthstare.scribereader.ui.home

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.FileUtils.loadFile
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.chemistry.DataSets
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.CardRenderer
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.FlashCardDB
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.parseElement
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.*
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.devilishtruthstare.scribereader.ImageLoader
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.DataSet
import com.devilishtruthstare.scribereader.ui.reader.Reader
import kotlinx.coroutines.withContext


@Composable
fun HomeScreen(context: Context) {
    val dataSets = remember { FlashCardDB.getDataSets(context) }

    val books = remember {
        RecordKeeper.getInstance(context).getBookListByRecency()
    }

    LazyColumn {

        item {
            Surface (
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
                    text = "Recent Books"
                )
            }
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(all = 20.dp)
            ) {
                items(books) { book ->
                    Log.d("Book", book.title)
                    CoverImage(book.title, context) {
                        val intent = Intent(context, Reader::class.java).apply {
                            putExtra(
                                context.resources.getString(R.string.EXTRA_BOOK_ID),
                                book.bookId
                            )
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }

        item {
            Surface (
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp)
                    .clickable {
                        val intent = Intent(context, DataSets::class.java)
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(all = 10.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        text = "Data Sets"
                    )
                    Icon(Icons.Default.Add, contentDescription = "")
                }
            }
        }

        items(dataSets) { dataSet ->
            ShowOneFromDataSet(context, dataSet)
        }
    }
}

@Composable
internal fun CoverImage(title: String, context: Context, onClick: () -> Unit) {
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

@Composable
internal fun ShowOneFromDataSet(context: Context, dataSet: DataSet) {
    val elementList = remember { JSONArray(loadFile(context, dataSet.fileLocation)) }
    var selectedElement by remember { mutableIntStateOf((0 until elementList.length()).random()) }
    val layoutJSON = remember { JSONObject(loadFile(context, dataSet.layoutFileLocation)) }

    Swippable(
        onSwipedLeft = {
            selectedElement = (0 until elementList.length()).random()
        },
        onSwipedRight = {
            selectedElement = (0 until elementList.length()).random()
        }
    ) {
        DisplayPreview(elementList.getJSONObject(selectedElement), layoutJSON)
    }

}

@Composable
internal fun DisplayPreview(selectedElement: JSONObject, layout: JSONObject) {
    val card = remember(layout, selectedElement) {
        parseElement(layout, selectedElement)
    }

    CardRenderer(card)
}

@Composable
internal fun Swippable(onSwipedLeft: () -> Unit, onSwipedRight: () -> Unit, children: @Composable () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onSwipedRight()
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onSwipedLeft()
                }
                else -> {}
            }
            false
        }
    )

    SwipeToDismissBox(
        modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
        state = dismissState,
        backgroundContent = {},
        content = {
            children()
        }
    )
}
