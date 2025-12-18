package com.devilishtruthstare.scribereader.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CheckBox
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.viewinterop.AndroidView
import com.devilishtruthstare.scribereader.pokemon.PokemonScreen
import com.devilishtruthstare.scribereader.pokemon.Regions
import com.devilishtruthstare.scribereader.ui.chemistry.DisplayDatasets
import com.devilishtruthstare.scribereader.ui.chemistry.flashcard.CardElement
import com.devilishtruthstare.scribereader.ui.home.HomeScreen
import com.devilishtruthstare.scribereader.ui.library.LibraryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScribeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val items = listOf("Home", "Library", "Pokemon", "Data", "Web")
    val icons = listOf(Icons.Default.Home, Icons.Default.Star, Icons.Default.Favorite, Icons.Default.Create, Icons.Default.Share)
    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = label) },
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
                0 -> HomeScreen()
                1 -> LibraryScreen()
                2 -> PokemonScreen()
                3 -> DisplayDatasets()
                4 -> Web()
            }
        }
    }
}

@Composable
fun CheckboxDropdown(
    options: List<String>,
    selected: Set<String>,
    onSelectionChange: (Set<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton (onClick = { expanded = true }) {
            Text("Pokemon")
        }

        DropdownMenu (
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row (verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = option in selected,
                                onCheckedChange = {
                                    val new = selected.toMutableSet()
                                    if (it) new.add(option) else new.remove(option)
                                    onSelectionChange(new)
                                }
                            )
                            Text(option)
                        }
                    },
                    onClick = {
                        val new = selected.toMutableSet()
                        if (option !in selected) new.add(option) else new.remove(option)
                        onSelectionChange(new)
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Web() {
    var url by remember { mutableStateOf("https://www.google.com") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        // Hide keyboard on submit handled automatically, or add focus manager if needed
                    }),
                    colors = TextFieldDefaults.colors(
                        //setting the text field background when it is focused
                        focusedContainerColor = MaterialTheme.colorScheme.surface,

                        //setting the text field background when it is unfocused or initial state
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,

                        //setting the text field background when it is disabled
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        )
                )
            },
            actions = {
                IconButton(onClick = { /* trigger reload or clear? */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload")
                }
            }
        )

        AndroidView(
            modifier = Modifier.weight(1f),
            factory = {
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            },
            update = {
                it.loadUrl(url)
            }
        )
    }
}