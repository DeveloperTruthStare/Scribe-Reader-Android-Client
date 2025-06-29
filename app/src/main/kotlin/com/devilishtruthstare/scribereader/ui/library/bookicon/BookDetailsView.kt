package com.devilishtruthstare.scribereader.ui.library.bookicon

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.jmdict.Dictionary
import com.devilishtruthstare.scribereader.jmdict.LibraryDB

class BookDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    book: Book
) : FrameLayout(context, attrs) {
    private val titleLabel: TextView
    private val dropDownMenu: AutoCompleteTextView
    private val currentChapterEditText: EditText
    private val currentSectionEditText: EditText
    private val generateAnkiDeckButton: Button
    init {
        LayoutInflater.from(context).inflate(R.layout.book_icon_details_view, this, true)
        titleLabel = findViewById(R.id.book_title)
        dropDownMenu = findViewById(R.id.dropdown_menu)
        val options = resources.getStringArray(R.array.dropdown_options)
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, options)
        currentChapterEditText = findViewById(R.id.current_chapter_edit_text)
        currentSectionEditText = findViewById(R.id.current_section_edit_text)
        generateAnkiDeckButton = findViewById(R.id.generate_anki_deck_button)

        titleLabel.text = book.title
        dropDownMenu.setAdapter(adapter)
        dropDownMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            Toast.makeText(context, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
        }
        currentChapterEditText.setText("${book.currentChapter}")
        currentSectionEditText.setText("${book.currentSection}")
        generateAnkiDeckButton.setOnClickListener {
            val uniqueTokens = LibraryDB.getInstance(context).getUniqueTokenList(book.bookId)
            Log.d("Flash Cards", "${uniqueTokens.size}")
        }
    }
}