package com.devilishtruthstare.scribereader.ui.chemistry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devilishtruthstare.scribereader.R
import kotlinx.serialization.json.Json
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp


class PeriodicTableQuizActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputStream = resources.openRawResource(R.raw.periodic_table)
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val json = Json { ignoreUnknownKeys = true }
        val elements: List<Element> = json.decodeFromString(jsonString)
        setContent {
            MaterialTheme {
                TableList(elements)
            }
        }
    }
}

@Composable
fun TableList(elements: List<Element>) {
    LazyColumn {
        items(elements) { element ->
            var isExpanded by remember { mutableStateOf(false) }
            Surface(
                modifier = Modifier.fillMaxWidth()
                                    .padding(all = 10.dp)
                                    .clickable { isExpanded = !isExpanded },
                shape = RoundedCornerShape(16.dp),
                color = Color.DarkGray
            ) {
                Column {
                    Row(modifier = Modifier.padding(all = 10.dp)) {
                        Text(
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            text = element.name
                        )
                        Text(
                            text = element.symbol,
                            color = Color.White
                        )
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(all = 10.dp),
                        text = element.summary,
                        color = Color.White,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                    )
                }
            }
        }
    }
}

@Composable
fun JPNText() {
    
}

@kotlinx.serialization.Serializable
data class Element(
    var name: String = "",
    var appearance: String? = "",
    var atomic_mass: Double? = 0.0,
    var boil: Double? = 0.0,
    var category: String? = "",
    var density: Double? = 0.0,
    var discovered_by: String? = "",
    var melt: Double? = 0.0,
    var molar_heat: Double? = 0.0,
    var named_by: String? = "",
    var number: Int = 0,
    var period: Int = -1,
    var group: Int = -1,
    var phase: String = "",
    var source: String = "",
    var bohr_model_image: String? = "",
    var bohr_model_3d: String? = "",
    var spectral_img: String? = "",
    var summary: String = "",
    var symbol: String = "",
    var xpos: Int = -1,
    var ypos: Int = -1,
    var wxpos: Int = -1,
    var wypos: Int = -1,
    var shells: List<Int>? = emptyList(),
    var electron_configuration: String? = "",
    var electron_configuration_schematic: String? = "",
    var electron_affinity: Double? = 0.0,
    var electronegativity_pauling: Double? = 0.0,
    var ionization_energies: List<Double>? = emptyList(),
    var cpk_hex: String? = "",
    var image: ElementImage?,
    var block: String? = ""
)

@kotlinx.serialization.Serializable
data class ElementImage(
    var title: String = "",
    var url: String = "",
    var attribution: String = ""
)