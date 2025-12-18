package com.devilishtruthstare.scribereader.pokemon

import android.R.attr.contentDescription
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.ui.CheckboxDropdown


@Composable
fun PokemonScreen() {
    val pokeapi = PokeAPI(LocalContext.current)
    pokeapi.copyDatabaseFromAssets()
    val pokemon = pokeapi.getListOfPokemon()
    val options = Regions.entries.map { it.name }

    var selected by remember { mutableStateOf(mutableSetOf<String>()) }
    var team = remember { mutableStateListOf<PokemonDBO?>(null, null, null, null, null, null) }
    var selectedMember by remember { mutableStateOf(0) }

    Column (modifier = Modifier.fillMaxSize()) {
        CheckboxDropdown(
            options = options,
            selected = selected
        ) { newSelection ->
            selected = newSelection.toMutableSet()
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items (selected.toList()) { item ->
                Text( text = item )
            }

            items (pokemon.toList()) { item ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        team[selectedMember] = pokemon[item.id-1]
                        if (selectedMember < 5) {
                            selectedMember++
                        }
                }) {
                    Text (text = item.name)
                }
            }
        }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PokemonPartyIcon(0, team[0], 0 == selectedMember) {
                    selectedMember = 0
                }
                PokemonPartyIcon(1, team[1], 1 == selectedMember) {
                    selectedMember = 1
                }
                PokemonPartyIcon(2, team[2], 2 == selectedMember) {
                    selectedMember = 2
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                PokemonPartyIcon(3, team[3], 3 == selectedMember) {
                    selectedMember = 3
                }
                PokemonPartyIcon(4, team[4], 4 == selectedMember) {
                    selectedMember = 4
                }
                PokemonPartyIcon(5, team[5], 5 == selectedMember) {
                    selectedMember = 5
                }
            }
        }
    }
}

@Composable
fun RowScope.PokemonPartyIcon(
    id: Int,
    pokemon: PokemonDBO?,
    active: Boolean,
    onTapped: (index: Int) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f),
        color = if (active) Color("#1f1fff".toColorInt()) else Color("#1f1f1f".toColorInt()),
        onClick = {
            onTapped(id)
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pokemon Icon
            if (pokemon != null) {
                PokemonImage(pokemon.id, modifier = Modifier.weight(1f))
            } else {
                PokemonImage(0, modifier = Modifier.weight(1f))
            }

            // Pokemon Name
            Text(
                text = pokemon?.name ?: "???",
                modifier = Modifier
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
                )

            // Types
            Row {
                if (pokemon != null) {
                    NumberedImage(pokemon.type1)
                    if (pokemon.type2 != null) {
                        NumberedImage(pokemon.type2)
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.PokemonImage(dexId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageBitmap = remember(dexId) {
        val path = "pokemon/$dexId.png"
        context.assets.open(path).use { input ->
            BitmapFactory.decodeStream(input)?.asImageBitmap()
        }
    }

    imageBitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = modifier
        )
    }
}

@Composable
fun RowScope.NumberedImage(typeId: Int) {
    val context = LocalContext.current

    // Convert number to drawable resource name (without extension)
    val resId = remember(typeId) {
        context.resources.getIdentifier("type" + typeId.toString(), "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            painter = painterResource(id = resId),
            contentDescription = "Image $typeId"
        )
    } else {
        // Optional: fallback if the resource doesn't exist
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text("Missing image: $typeId")
        }
    }
}
