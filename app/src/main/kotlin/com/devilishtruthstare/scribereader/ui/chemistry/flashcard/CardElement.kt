package com.devilishtruthstare.scribereader.ui.chemistry.flashcard

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import org.json.JSONObject

sealed class CardElement(open val modifierConfig: ModifierConfig = ModifierConfig()) {
    data class Text(
        val value: String,
        val color: Color? = null,
        override val modifierConfig: ModifierConfig = ModifierConfig(),
        val showWhenCollapsed: Boolean = true
    ) : CardElement(modifierConfig)

    data class Row(val children: List<CardElement>, override val modifierConfig: ModifierConfig = ModifierConfig()) : CardElement(modifierConfig)
    data class Column(val children: List<CardElement>, override val modifierConfig: ModifierConfig = ModifierConfig()) : CardElement(modifierConfig)

    data class Surface(
        val children: List<CardElement>,
        val shape: Shape? = null,
        val color: Color? = null,
        val expandable: Boolean = false,
        override val modifierConfig: ModifierConfig = ModifierConfig()
    ) : CardElement(modifierConfig)
}

internal fun parseColor(colorName: String?): Color? {
    return when(colorName?.lowercase()) {
        "darkgray", "dark_gray", "darkgrey" -> Color.DarkGray
        "white" -> Color.White
        // Add more colors as needed
        else -> null
    }
}

internal fun parseShape(json: JSONObject?): Shape? {
    if (json == null) return null
    val cornerRadius = json.optInt("roundedCornerShape", 0)
    return if (cornerRadius > 0) RoundedCornerShape(cornerRadius.dp) else null
}

fun parseElement(json: JSONObject, data: JSONObject? = null): CardElement {
    val modifierConfig = parseModifierConfig(json.optJSONObject("modifier"))
    val color = parseColor(json.optString("color", ""))

    return when (val type = json.getString("type")) {
        "text" -> {
            val raw = json.getString("value")
            val showWhenCollapsed = json.optBoolean("showWhenCollapsed", true)
            CardElement.Text(
                value = expandVariables(raw, data),
                color = color,
                modifierConfig = modifierConfig,
                showWhenCollapsed = showWhenCollapsed
            )
        }
        "row", "column" -> {
            val childrenArray = json.getJSONArray("children")
            val children = (0 until childrenArray.length()).map { i ->
                parseElement(childrenArray.getJSONObject(i), data)
            }
            if (type == "row") CardElement.Row(children, modifierConfig)
            else CardElement.Column(children, modifierConfig)
        }
        "surface" -> {
            val childrenArray = json.getJSONArray("children")
            val children = (0 until childrenArray.length()).map { i ->
                parseElement(childrenArray.getJSONObject(i), data)
            }
            val shape = parseShape(json.optJSONObject("shape"))
            val expandable = json.optBoolean("expandable", false)
            CardElement.Surface(children, shape, color, expandable, modifierConfig)
        }
        else -> throw IllegalArgumentException("Unknown type: $type")
    }
}

internal fun expandVariables(template: String, data: JSONObject?): String {
    return Regex("\\{\\{(.*?)\\}\\}").replace(template) { match ->
        val key = match.groupValues[1].trim()
        val value = data?.opt(key) ?: return@replace match.value
        value.toString()
    }
}
