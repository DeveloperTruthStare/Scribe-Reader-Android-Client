package com.devilishtruthstare.scribereader.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.chemistry.PeriodicTableQuizActivity
import com.devilishtruthstare.scribereader.ui.library.bookicon.BookView

class HomeFragment : Fragment(R.layout.home_fragment) {
    private lateinit var recentBooksContainer: LinearLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentBooksContainer = view.findViewById(R.id.home_recent_books_container)
        val ptableButton = view.findViewById<TextView>(R.id.periodTableButton)
        ptableButton.setOnClickListener {
            val intent = Intent(requireContext(), PeriodicTableQuizActivity::class.java)
            startActivity(intent)
        }
        loadRecentBooks()
    }

    private fun loadRecentBooks() {
        recentBooksContainer.removeAllViews()

        val books = RecordKeeper.getInstance(requireContext()).getBookListByRecency()

        for (book in books) {
            val bookIcon = BookView(requireContext(), book = book)
            recentBooksContainer.addView(bookIcon)
        }
    }
}