package com.devilishtruthstare.scribereader.mian.library

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.database.RecordKeeper
import java.io.File


class LibraryFragment : Fragment(R.layout.fragment_library) {
    private lateinit var recordKeeper: RecordKeeper
    private lateinit var libraryContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recordKeeper = RecordKeeper.getInstance(requireContext())

        val outputDir = File(requireContext().filesDir, "books")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryContainer = view.findViewById(R.id.libraryContainer)

        refreshLibraryView()
    }

    private fun refreshLibraryView() {
        val books = recordKeeper.getBookList()
        if (books.isEmpty()) return

        libraryContainer.removeAllViews()
        var previousAuthor = ""
        var authorContainer: AuthorView? = null

        for (book in books) {
            if (previousAuthor != book.author) {
                previousAuthor = book.author
                authorContainer = AuthorView(requireContext()).apply {
                    setAuthor(book.author)
                }
                libraryContainer.addView(authorContainer)
            }
            authorContainer?.addBook(book)
        }
    }
}