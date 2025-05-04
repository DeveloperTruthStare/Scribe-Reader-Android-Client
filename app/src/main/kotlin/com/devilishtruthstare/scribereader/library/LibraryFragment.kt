package com.devilishtruthstare.scribereader.library

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.database.RecordKeeper
import java.io.File


class LibraryFragment : Fragment(R.layout.fragment_library) {
    private lateinit var recordKeeper: RecordKeeper
    private lateinit var libraryContainer: GridLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordKeeper = RecordKeeper(requireContext())
        libraryContainer = view.findViewById(R.id.libraryContainer)

        refreshLibraryView()
        val outputDir = File(requireContext().filesDir, "books")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    private fun refreshLibraryView() {
        libraryContainer.removeAllViews()

        val books = recordKeeper.getBookList()

        for(book in books) {
            val bookIcon = BookView(requireContext(), book = book)
            libraryContainer.addView(bookIcon)
        }
    }
}