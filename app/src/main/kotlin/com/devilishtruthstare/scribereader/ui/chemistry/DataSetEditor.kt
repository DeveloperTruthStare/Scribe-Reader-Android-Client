@file:JvmName("DataSetEditorKt")

package com.devilishtruthstare.scribereader.ui.chemistry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.CardRenderer
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.parseElement
import com.devilishtruthstare.scribereader.ui.theme.ScribeTheme
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DataSetEditor : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var dataSetJSON = loadFile(intent.getStringExtra("DATA_SET_LOCATION")!!)
        val dataArray = JSONArray(dataSetJSON)

        val layoutFileLocation = intent.getStringExtra("LAYOUT_LOCATION")!!
        var layoutLocationJSON = loadFile(layoutFileLocation)
        val layoutData = JSONObject(layoutLocationJSON)

        setContent {
            ScribeTheme {
                Surface (
                    color = MaterialTheme.colorScheme.surface
                ) {
                    var data by remember { mutableStateOf(dataArray) }
                    var layout by remember { mutableStateOf(layoutData) }

                    TabbedPagerView(data, layout) { newLayout ->
                        saveStringToFile(layoutFileLocation, newLayout)
                        layout = JSONObject(newLayout)
                    }
                }
            }
        }
    }

    private fun loadFile(filePath: String): String {
        return if (filePath.substring(0, "assets://".length) == "assets://") {
            assets.open(filePath.substring("assets://".length)).bufferedReader().use { it.readText() }
        } else {
            File(filesDir, "layouts/${filePath}").readText()
        }
    }
    private fun saveStringToFile(filename: String, content: String) {
        val file = File(filesDir, "layouts/$filename")
        file.parentFile?.mkdirs()
        file.writeText(content)
    }
}

@Composable
fun TabbedPagerView(data: JSONArray, layout: JSONObject, onLayoutChanged: (String) -> Unit) {
    val tabs = listOf("Data Set", "Layout", "Preview")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Column {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(title) }
                )
            }
        }

        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            when (page) {
                0 -> DisplayDataset(data)
                1 -> EditLayout(layout, onLayoutChanged)
                2 -> DisplayPreview(data, layout)
            }
        }
    }
}

@Composable
fun DisplayDataset(data: JSONArray) {
    val types = getTypes(data)

    Surface(modifier = Modifier.padding(all = 10.dp)) {
        LazyColumn {
            items(types.keys.toList()) { type ->
                Row{
                    Text(text = type)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(text = types[type].toString())
                }
            }
        }
    }
}

internal fun getTypes(data: JSONArray): Map<String, String> {
    if (data.length() == 0) {
        return emptyMap()
    }
    val obj = data.getJSONObject(0)
    return getTypes(obj)
}
internal fun getTypes(obj: JSONObject): Map<String, String> {
    return buildMap {
        obj.keys().forEach { key ->
            val value = obj[key]
            val type = getType(value)
            put(key, type)
        }
    }
}
internal fun getType(obj: Any): String {
    return when (obj) {
        is Int -> "int"
        is Long -> "long"
        is Double -> "double"
        is Boolean -> "boolean"
        is String -> "string"
        is JSONObject -> "object"
        is JSONArray -> "array"
        else -> "unknown"
    }
}

@Composable
fun EditLayout(layout: JSONObject, onSave: (String) -> Unit) {
    var textContent by remember { mutableStateOf(layout.toString(2)) }
    var textChanged by remember { mutableStateOf(false) }
    var lastText by remember { mutableStateOf(textContent) }

    Box(modifier = Modifier
        .fillMaxSize()
        .imePadding()
    ) {
        TextField(
            value = textContent,
            onValueChange = {
                textChanged = it != lastText
                textContent = it
            },
            singleLine = false,
            modifier = Modifier
                .fillMaxSize()
        )

        if (textChanged) {
            ExtendedFloatingActionButton(
                text = {},
                icon = { Icon(Icons.Filled.Done, contentDescription = "") },
                onClick = {
                    textChanged = false
                    lastText = textContent
                    onSave(textContent)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun DisplayPreview(data: JSONArray, layout: JSONObject) {
    val dataList = remember { (0 until data.length()).map { i -> data.getJSONObject(i) } }

    LazyColumn {
        items(dataList) { element ->
            val card = remember(layout) { parseElement(layout, element) }
            CardRenderer(card)
        }
    }
}
