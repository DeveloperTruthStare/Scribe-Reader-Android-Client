package com.devilishtruthstare.scribereader.ui.library

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.ui.uploader.UploadFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private lateinit var recordKeeper: RecordKeeper
    private lateinit var libraryContainer: LinearLayout
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val sections: MutableMap<String, AuthorView> = mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("LibraryFragment", "OnCreate")
        recordKeeper = RecordKeeper.getInstance(requireContext())
        filePickerLauncher = createFilePickerLauncher()


        val outputDir = File(requireContext().filesDir, "books")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryContainer = view.findViewById(R.id.libraryContainer)
        refreshLibraryView()
        view.findViewById<FloatingActionButton>(R.id.fab_main).setOnClickListener {
            filePickerLauncher.launch("application/epub+zip")
        }
    }

    private fun refreshLibraryView() {
        Log.d("LibraryFragment", "Start")
        val books = recordKeeper.getBookList()
        if (books.isEmpty()) return

        libraryContainer.removeAllViews()
        sections.clear()

        for (book in books) {
            sections.getOrPut(book.author) { AuthorView(requireContext()).apply { setAuthor(book.author)} }.addBook(book)
        }

        for (section in sections.values) {
            if (section.hasBooks()) {
                libraryContainer.addView(section)
            }
        }
    }

    private fun createFilePickerLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val uploadFragment = UploadFragment()
                uploadFragment.setBook(uri)

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, uploadFragment)
                    .commit()
            }
        }
    }
}