package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.dictionary.ui.DictionaryView


class TokenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    token: Token
) : FrameLayout(context, attrs) {

    init {
        val button = LayoutInflater.from(context).inflate(R.layout.component_token, this, true)

        val textView = button.findViewById<TextView>(R.id.token_text)
        val underline = button.findViewById<View>(R.id.underline)

        textView.text = token.surface
        textView.textSize = 24f

        if (token.features.isEmpty() ||
            token.features[0] in Token.IGNORED_MARKERS) {
            underline.visibility = GONE
        } else {
            button.setOnClickListener { view ->
                showReadingPopup(view, token)
            }

            button.setOnLongClickListener { view ->
                showDefinitionPopup(view, token)
                true
            }
            val underlineColor = when (token.features[0]) {
                Token.NOUN_MARKER -> "#d97706"           // amber for nouns
                Token.PRE_NOUN_ADJECTIVAL -> "#f59e0b"   // lighter amber
                Token.CONJUNCTION_MARKER -> "#10b981"    // teal for conjunctions
                Token.VERB_MARKER -> "#3b82f6"           // blue for verbs
                Token.CONJUGATION_MARKER -> "#60a5fa"    // lighter blue
                Token.PREFIX_MARKER -> "#8b5cf6"         // violet for prefixes
                Token.INTERJECTION_MARKER -> "#ec4899"   // pink for interjections
                Token.I_ADJECTIVE_MARKER -> "#f43f5e"    // red for i-adjectives
                Token.ADVERB_MARKER -> "#14b8a6"         // cyan for adverbs
                else -> "#6b7280"                  // gray fallback
            }

            underline.setBackgroundColor(underlineColor.toColorInt())
        }
    }

    private fun showDefinitionPopup(anchorView: View, token: Token) {
        val definitionView = DictionaryView(context, startingSearch = Token.getSearchTerm(token))

        // Convert dp to pixels
        val marginWidth = (20 * context.resources.displayMetrics.density).toInt()

        // Get screen dimensions
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calculate popup dimensions
        val popupWidth = screenWidth - (2 * marginWidth)  // Full width minus margins
        val popupHeight = screenHeight * 3 / 4

        // Create PopupWindow with calculated dimensions
        val definitionWindow = PopupWindow(definitionView, popupWidth, popupHeight, true)

        definitionWindow.isOutsideTouchable = true

        definitionWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 0)
    }
    private fun showReadingPopup(anchorView: View, token: Token) {
        val view = TokenReadingView(context, token = token)

        // Measure the popup width to center it properly
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val popupWidth = view.measuredWidth

        // Get the location of the button (anchorView)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorCenterX = location[0] + anchorView.width / 2

        val xOffset = anchorCenterX - popupWidth / 2

        val anchorCenterY = location[1] + anchorView.width / 2
        val yOffset = anchorCenterY + anchorView.measuredHeight / 2

        val popupWindow = PopupWindow(
            view,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            true
        )

        // Set rounded corners for the popup window background
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(anchorView.context,
                R.drawable.rounded_popup
            ))
        popupWindow.isOutsideTouchable = true

        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)
    }
}