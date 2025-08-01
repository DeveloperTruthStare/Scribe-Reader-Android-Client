package com.devilishtruthstare.scribereader.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.devilishtruthstare.scribereader.FileUtils.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devilishtruthstare.scribereader.ui.theme.ScribeTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.ui.chemistry.DataSets
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.CardRenderer
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.FlashCardDB
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.parseElement
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.SwipeToDismissBoxValue


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScribeTheme {
                MyScreen(this@MainActivity)
            }
        }
    }
}

@Composable
fun MyScreen(context: Context) {
    val items = listOf("Home", "Library")
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = label) },
                        label = { Text(label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedIndex) {
                0 -> HomeScreen(context)
                1 -> Text("Library Screen")
            }
        }
    }
}

@Composable
internal fun HomeScreen(context: Context) {
    val elementsDataset = remember { FlashCardDB.getDataSets(context)[0] }
    val elementList = remember { JSONArray(loadFile(context, elementsDataset.fileLocation)) }

    val layoutJSON = remember { JSONObject(loadFile(context, elementsDataset.layoutFileLocation)) }

    Column {
        Button(onClick = {
            val intent = Intent(context, DataSets::class.java)
            context.startActivity(intent)
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)) {
            Text("View Data Sets")
        }

        var selectedElement by remember { mutableIntStateOf(0) }

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
}

@Composable
fun DisplayPreview(selectedElement: JSONObject, layout: JSONObject) {
    val card = remember(layout, selectedElement) {
        parseElement(layout, selectedElement)
    }

    CardRenderer(card)
}

@Composable
fun Swippable(onSwipedLeft: () -> Unit, onSwipedRight: () -> Unit, children: @Composable () -> Unit) {
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
        state = dismissState,
        backgroundContent = {},
        content = {
            children()
        }
    )
}
