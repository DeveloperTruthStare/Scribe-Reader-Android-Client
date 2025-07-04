package com.devilishtruthstare.scribereader.ui.library

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.book.RecordKeeper
import com.devilishtruthstare.scribereader.databinding.FragmentLibraryBinding
import com.devilishtruthstare.scribereader.editor.EditorActivity
import com.devilishtruthstare.scribereader.ui.reader.Reader
import com.devilishtruthstare.scribereader.ui.uploader.UploadFragment
import java.io.File

class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recordKeeper: RecordKeeper
    private lateinit var libraryContainer: LinearLayout
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val sections: MutableMap<String, AuthorView> = mutableMapOf()

    private var isMenuOpen = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)

        recordKeeper = RecordKeeper.getInstance(requireContext())
        filePickerLauncher = createFilePickerLauncher()

        val outputDir = File(requireContext().filesDir, "books")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        binding.fabMain.setOnClickListener {
            isMenuOpen = !isMenuOpen
            binding.fabOption1.visibility = if (isMenuOpen) View.VISIBLE else View.GONE
            binding.fabOption2.visibility = if (isMenuOpen) View.VISIBLE else View.GONE
        }
        libraryContainer = binding.libraryContainer
        refreshLibraryView()
        binding.fabOption1.setOnClickListener {
            filePickerLauncher.launch("application/epub+zip")
        }

        binding.fabOption2.setOnClickListener {
            val intent = Intent(context, EditorActivity::class.java).apply {
                data = null
            }
            requireContext().startActivity(intent)
        }
        return binding.root
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
