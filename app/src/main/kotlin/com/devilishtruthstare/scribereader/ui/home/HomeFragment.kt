package com.devilishtruthstare.scribereader.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.chemistry.DataSets
import com.devilishtruthstare.scribereader.ui.library.bookicon.BookView
import androidx.compose.foundation.lazy.items


class HomeFragment : Fragment(R.layout.home_fragment) {
    private lateinit var recentBooksContainer: LinearLayout
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentBooksContainer = view.findViewById(R.id.home_recent_books_container)
        val ptableButton = view.findViewById<TextView>(R.id.periodTableButton)
        ptableButton.setOnClickListener {
            val intent = Intent(requireContext(), DataSets::class.java)
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


@Composable
fun HomeScreen(navToPeriodicTable: () -> Unit) {
    val context = LocalContext.current
    val books = remember {
        RecordKeeper.getInstance(context).getBookListByRecency()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Periodic Table",
            modifier = Modifier
                .clickable { navToPeriodicTable() }
                .padding(8.dp),
            color = Color.Blue
        )

        Spacer(Modifier.height(16.dp))

        LazyRow {
            items(books) { book ->
                AndroidView(
                    factory = { ctx -> BookView(ctx, book = book) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}
