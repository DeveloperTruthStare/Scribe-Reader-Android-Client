package com.devilishtruthstare.scribereader.ui.chemistry.flashcard

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject

data class ModifierConfig(
    val fillMaxWidth: Boolean = false,
    val fillMaxHeight: Boolean = false,
    val padding: Int? = null,
    val weight: Float? = null
)

@SuppressLint("ModifierFactoryExtensionFunction")
internal fun ModifierConfig.toModifier(): Modifier {
    var modifier: Modifier = Modifier
    if (fillMaxWidth) modifier = modifier.fillMaxWidth()
    if (fillMaxHeight) modifier = modifier.fillMaxHeight()
    padding?.let { modifier = modifier.padding(it.dp) }
    return modifier
}


internal fun parseModifierConfig(json: JSONObject?): ModifierConfig {
    if (json == null) return ModifierConfig()

    val rawWeight = json.optDouble("weight")
    val weight = if (!rawWeight.isNaN() && rawWeight > 0) rawWeight.toFloat() else null

    return ModifierConfig(
        fillMaxWidth = json.optBoolean("fillMaxWidth", false),
        fillMaxHeight = json.optBoolean("fillMaxHeight", false),
        padding = json.optInt("padding").takeIf { it != 0 },
        weight = weight
    )
}