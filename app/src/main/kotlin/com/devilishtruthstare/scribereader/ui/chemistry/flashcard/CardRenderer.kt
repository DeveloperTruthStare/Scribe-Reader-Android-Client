package com.devilishtruthstare.scribereader.ui.chemistry.flashcard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun CardRenderer(element: CardElement) {
    when (element) {
        is CardElement.Text -> {
            Text(
                text = element.value,
                modifier = element.modifierConfig.toModifier(),
                color = element.color ?: LocalContentColor.current
            )
        }
        is CardElement.Row -> Row {
            element.children.forEach {
                CardRenderer(it)
            }
        }
        is CardElement.Column -> Column {
            element.children.forEach {
                CardRenderer(it)
            }
        }
        is CardElement.Surface -> {
            if (element.expandable) {
                var isExpanded by remember(element) { mutableStateOf(false) }
                Surface(
                    modifier = element.modifierConfig.toModifier()
                        .clickable { isExpanded = !isExpanded },
                    shape = element.shape ?: RectangleShape,
                    color = element.color ?: MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column {
                        element.children.forEach { child ->
                            // For Text children, respect showWhenCollapsed
                            if (child is CardElement.Text && !child.showWhenCollapsed && !isExpanded) {
                                // Skip rendering when collapsed
                            } else {
                                CardRenderer(child)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    modifier = element.modifierConfig.toModifier(),
                    shape = element.shape ?: RectangleShape,
                    color = element.color ?: MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column {
                        element.children.forEach { CardRenderer(it) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RowScope.CardRenderer(element: CardElement) {
    var modifier = element.modifierConfig.toModifier()
    element.modifierConfig.weight?.let {
        modifier = modifier.weight(it)
    }

    when (element) {
        is CardElement.Text -> Text(element.value, modifier = modifier, color = element.color ?: LocalContentColor.current)
        is CardElement.Row -> Row(modifier = modifier) {
            element.children.forEach { CardRenderer(it) }
        }
        is CardElement.Column -> Column(modifier = modifier) {
            element.children.forEach { CardRenderer(it) }
        }
        is CardElement.Surface -> CardRenderer(element) // surface handles its own modifier
    }
}

@Composable
internal fun ColumnScope.CardRenderer(element: CardElement) {
    var modifier = element.modifierConfig.toModifier()
    element.modifierConfig.weight?.let {
        modifier = modifier.weight(it)
    }

    when (element) {
        is CardElement.Text -> Text(element.value, modifier = modifier, color = element.color ?: LocalContentColor.current)
        is CardElement.Row -> Row(modifier = modifier) {
            element.children.forEach { CardRenderer(it) }
        }
        is CardElement.Column -> Column(modifier = modifier) {
            element.children.forEach { CardRenderer(it) }
        }
        is CardElement.Surface -> CardRenderer(element) // surface handles its own modifier
    }
}
