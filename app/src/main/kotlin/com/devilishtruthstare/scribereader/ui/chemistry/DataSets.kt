package com.devilishtruthstare.scribereader.ui.chemistry

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.FlashCardDB
import com.devilishtruthstare.scribereader.utils.NodeElement
import com.devilishtruthstare.scribereader.utils.TraverseNodes

internal fun onDataSetSelected(uri: Uri) {

}

@Composable
fun DisplayDatasets() {
    val context = LocalContext.current
    var dataSets = remember { FlashCardDB.getDataSets(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let{ onDataSetSelected(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn {
            item {
                Surface (
                    shape = RoundedCornerShape(10.dp),
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth().padding(all = 10.dp)
                        .clickable {
                            launcher.launch("*/*")
                        }
                ) {
                    Text(
                        text = "Import New Data Set",
                        color = Color.Green,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(dataSets) { dataSet ->
                Text(
                    text = dataSet.name,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 10.dp)
                        .clickable {
                            val intent = Intent(context, DataSetEditor::class.java)
                            intent.putExtra("DATA_SET_LOCATION", dataSet.fileLocation)
                            intent.putExtra("LAYOUT_LOCATION", dataSet.layoutFileLocation)
                            context.startActivity(intent)
                        },
                    textAlign = TextAlign.Center
                )
            }
            item {
                Column {

                    var textContent by remember { mutableStateOf("") }

                    TextField(
                        value = textContent,
                        onValueChange = {
                            textContent = it
                        },
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxSize()
                    )

                    ParsedText(textContent = textContent)

                }
            }
        }
    }
}

@Composable
fun ParsedText(textContent: String) {
    var document = TraverseNodes(textContent)
    var result = parseChildren(document)
    Text(text = result)
}

fun parseChildren(node: NodeElement, indent: Int = 0): String {
    var result = " ".repeat(indent*4) + node.tagName
    if (!node.attributes.isEmpty()) {
        // Add Attributes
    }

    result += "\n"

    for (child in node.children) {
        result += parseChildren(child, indent+1)
    }

    return result
}