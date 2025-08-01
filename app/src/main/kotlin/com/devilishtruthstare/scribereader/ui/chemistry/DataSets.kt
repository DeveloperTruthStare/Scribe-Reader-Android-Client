package com.devilishtruthstare.scribereader.ui.chemistry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.FlashCardDB
import com.devilishtruthstare.scribereader.ui.theme.ScribeTheme

class DataSets : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScribeTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    DisplayDatasets(this)
                }
            }
        }
    }
}

@Composable
fun DisplayDatasets(context: Context) {
    var dataSets = remember { FlashCardDB.getDataSets(context) }
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
        }
    }
}