package com.devilishtruthstare.scribereader.reader.content

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Content
import com.google.android.flexbox.FlexboxLayout
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.database.FlashCard
import com.devilishtruthstare.scribereader.dictionary.DictionaryView
import androidx.core.graphics.toColorInt


class TextContentHolder(
    itemView: View,
    private val context: Context,
) : RecyclerView.ViewHolder(itemView) {
    companion object {
        private const val PUNCTUATION_MARKER = "記号"
        private const val PARTICLE_MARKER = "助詞"
        private const val CONJUGATION_MARKER = "助動詞"
        private const val PRE_NOUN_ADJECTIVAL = "連体詞"
        private const val NOUN_MARKER = "名詞"
        private const val VERB_MARKER = "動詞"
        private const val AUX_VERB_MARKER = "助動詞"
        private const val CONJUNCTION_MARKER = "接続詞"
        private const val I_ADJECTIVE_MARKER = "形容詞"
        private const val ADVERB_MARKER = "副詞"
        private const val PREFIX_MARKER = "接頭詞"
        private const val INTERJECTION_MARKER = "感動詞"
        private const val FILLER_MARKER = "フィラー"
    }
    private val textContainer: FlexboxLayout = itemView.findViewById(R.id.container)
    private val ttsButton: ImageView = itemView.findViewById(R.id.tts_button)

    private lateinit var definitionView: View

    private lateinit var flashCard: FlashCard
    fun bind(section: Content) {
        textContainer.removeAllViews()
        for(token in section.tokens) {
            val button = LayoutInflater.from(itemView.context).inflate(R.layout.component_token, null)
            val textView = button.findViewById<TextView>(R.id.token_text)
            val underline = button.findViewById<View>(R.id.underline)

            textView.text = token.surface
            textView.textSize = 24f

            if (token.features.isEmpty() ||
                token.features[0] in listOf(PUNCTUATION_MARKER, FILLER_MARKER, PARTICLE_MARKER, AUX_VERB_MARKER)) {
                underline.visibility = View.GONE
            } else {
                button.setOnClickListener { view ->
                    showReadingPopup(view, token)
                }

                button.setOnLongClickListener { view ->
                    showDefinitionPopup(view, token)
                    true
                }
                val underlineColor = when (token.features[0]) {
                    NOUN_MARKER -> "#d97706"           // amber for nouns
                    PRE_NOUN_ADJECTIVAL -> "#f59e0b"   // lighter amber
                    CONJUNCTION_MARKER -> "#10b981"    // teal for conjunctions
                    VERB_MARKER -> "#3b82f6"           // blue for verbs
                    CONJUGATION_MARKER -> "#60a5fa"    // lighter blue
                    PREFIX_MARKER -> "#8b5cf6"         // violet for prefixes
                    INTERJECTION_MARKER -> "#ec4899"   // pink for interjections
                    I_ADJECTIVE_MARKER -> "#f43f5e"    // red for i-adjectives
                    ADVERB_MARKER -> "#14b8a6"         // cyan for adverbs
                    else -> "#6b7280"                  // gray fallback
                }

                underline.setBackgroundColor(underlineColor.toColorInt())
            }
            textContainer.addView(button)
        }
        flashCard = FlashCard(context)
        ttsButton.setOnClickListener {
            section.onPlaySoundClick()
        }
    }

    private fun showDefinitionPopup(anchorView: View, token: Token) {
        // Create UI
        definitionView = DictionaryView(context, startingSearch = Token.getSearchTerm(token))
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
        // Inflate the popup layout
        val popupView = LayoutInflater.from(anchorView.context).inflate(R.layout.popup_token_details, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set rounded corners for the popup window background
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(anchorView.context,
                R.drawable.rounded_popup
            ))
        popupWindow.isOutsideTouchable = true

        val readingText = popupView.findViewById<TextView>(R.id.reading)
        val baseWord = popupView.findViewById<TextView>(R.id.word)
        val partOfSpeech = popupView.findViewById<TextView>(R.id.partOfSpeech)

        if (token.features.size >= 8) {
            readingText.text = token.features[7]
        } else {
            readingText.visibility = View.GONE
        }

        if (token.features.size >= 7) {
            baseWord.text = token.features[6]
        } else {
            baseWord.visibility = View.GONE
        }

        if (token.features.isNotEmpty()) {
            partOfSpeech.text = token.features[0]
        } else {
            partOfSpeech.visibility = View.GONE
        }

        // Measure the popup width to center it properly
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        // Get the location of the button (anchorView)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorCenterX = location[0] + anchorView.width / 2

        // Calculate x position to center popup on the button
        val xOffset = anchorCenterX - popupWidth / 2

        val anchorCenterY = location[1] + anchorView.width / 2
        val yOffset = anchorCenterY + anchorView.measuredHeight / 2

        // Show the popup window offset from the button
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, xOffset, yOffset)


        Log.d("TempToken", "${token.features}")
    }
}
