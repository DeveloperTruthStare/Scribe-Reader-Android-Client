package com.devilishtruthstare.scribereader.mian.library.bookicon

import android.content.Context
import android.content.Intent
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
import com.devilishtruthstare.scribereader.dictionary.JMDict
import com.devilishtruthstare.scribereader.reader.Reader

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
    private val readBookButton: Button
    init {
        LayoutInflater.from(context).inflate(R.layout.book_icon_details_view, this, true)
        titleLabel = findViewById(R.id.book_title)
        dropDownMenu = findViewById(R.id.dropdown_menu)
        val options = resources.getStringArray(R.array.dropdown_options)
        val adapter = ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, options)
        currentChapterEditText = findViewById(R.id.current_chapter_edit_text)
        currentSectionEditText = findViewById(R.id.current_section_edit_text)
        generateAnkiDeckButton = findViewById(R.id.generate_anki_deck_button)
        readBookButton = findViewById(R.id.read_book_button)

        titleLabel.text = book.title
        dropDownMenu.setAdapter(adapter)
        dropDownMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedOption = options[position]
            Toast.makeText(context, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
        }
        currentChapterEditText.setText("${book.currentChapter}")
        currentSectionEditText.setText("${book.currentSection}")
        generateAnkiDeckButton.setOnClickListener {
            val uniqueTokens = JMDict.getInstance(context).getUniqueTokenList(book.bookId)
            for(token in uniqueTokens) {
                Log.d("Unique Tokens: ${book.title}", token)
            }
        }
        readBookButton.setOnClickListener {
            val intent = Intent(context, Reader::class.java).apply {
                putExtra(Reader.Companion.EXTRA_BOOK_ID, book.bookId)
            }
            context.startActivity(intent)
        }
    }
}