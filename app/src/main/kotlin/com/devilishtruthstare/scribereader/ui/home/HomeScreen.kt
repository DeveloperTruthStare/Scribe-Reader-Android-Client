package com.devilishtruthstare.scribereader.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.CardRenderer
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.FlashCardDB
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.parseElement
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.DataSet
import com.devilishtruthstare.scribereader.ui.components.CoverImage
import com.devilishtruthstare.scribereader.ui.reader.Reader

@Composable
fun HomeScreen() {
    val context = LocalContext.current
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
                    CoverImage(book.title) {
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
            Column {
                val elements = remember { JSONArray(loadFile(context, dataSets[0].fileLocation)) }
                val layoutJSON = remember { JSONObject(loadFile(context, dataSets[0].layoutFileLocation)) }

                var selectedElement by remember { mutableIntStateOf((0 until elements.length()).random()) }

                PeriodicTableGridWithPositioning(
                    elements = elements,
                    selectedElement = selectedElement
                ) { element ->
                    selectedElement = element.optInt("number", 1)-1
                }

                Swippable(
                    onSwipedLeft = {
                        selectedElement = (0 until elements.length()).random()
                    },
                    onSwipedRight = {
                        selectedElement = (0 until elements.length()).random()
                    }
                ) {
                    DisplayPreview(elements.getJSONObject(selectedElement), layoutJSON)
                }

            }
        }

        items(dataSets.subList(1, dataSets.size)) { dataSet ->
            ShowOneFromDataSet(dataSet) { newSelected ->

            }
        }
    }
}

@Composable
fun PeriodicTableGridWithPositioning(
    elements: JSONArray,
    selectedElement: Int = 0,
    columns: Int = 18,
    rows: Int = 10,
    spacing: Dp = 2.dp,
    onElementClick: (JSONObject) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val scope = this

        val totalWidth = maxWidth
        val itemSize = remember(totalWidth) {
            (totalWidth - spacing * (columns - 1)) / columns
        }
        val totalHeight = itemSize * rows + spacing * (rows - 1)

        Box(
            modifier = Modifier
                .height(totalHeight)
                .fillMaxWidth()
        ) {
            for (i in 0 until elements.length()) {
                val element = elements.getJSONObject(i)
                val group = element.optInt("xpos", 1) - 1 // zero-based
                val period = element.optInt("ypos", 1) - 1 // zero-based

                Box(
                    modifier = Modifier
                        .size(itemSize)
                        .offset(
                            x = (itemSize + spacing) * group,
                            y = (itemSize + spacing) * period
                        )
                        .background(if (i == selectedElement) Color.Green else Color.LightGray, RoundedCornerShape(8.dp))
                        .clickable { onElementClick(element) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = element.optString("symbol", "?"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}



@Composable
internal fun ShowOneFromDataSet(dataSet: DataSet, onOneChanged: (Int) -> Unit) {
    val context = LocalContext.current

    val elementList = remember { JSONArray(loadFile(context, dataSet.fileLocation)) }
    var selectedElement by remember { mutableIntStateOf((0 until elementList.length()).random()) }
    val layoutJSON = remember { JSONObject(loadFile(context, dataSet.layoutFileLocation)) }

    Swippable(
        onSwipedLeft = {
            selectedElement = (0 until elementList.length()).random()
            onOneChanged(selectedElement)
        },
        onSwipedRight = {
            selectedElement = (0 until elementList.length()).random()
            onOneChanged(selectedElement)
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
