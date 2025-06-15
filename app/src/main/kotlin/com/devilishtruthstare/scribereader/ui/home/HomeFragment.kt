package com.devilishtruthstare.scribereader.ui.home

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.database.RecordKeeper
import com.devilishtruthstare.scribereader.ui.library.bookicon.BookView

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var recentBooksContainer: LinearLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentBooksContainer = view.findViewById(R.id.home_recent_books_container)

        loadRecentBooks()
    }

    private fun loadRecentBooks() {
        recentBooksContainer.removeAllViews()

        val books = RecordKeeper.Companion.getInstance(requireContext()).getBookListByRecency()

        for (book in books) {
            val bookIcon = BookView(requireContext(), book = book)
            recentBooksContainer.addView(bookIcon)
        }
    }
}