package com.devilishtruthstare.scribereader

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devilishtruthstare.scribereader.flashcards.AnkiFragment
import com.devilishtruthstare.scribereader.home.HomeFragment
import com.devilishtruthstare.scribereader.library.LibraryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val homeFragment: HomeFragment = HomeFragment()
    private val ankiFragment: AnkiFragment = AnkiFragment()
    private val libraryFragment: LibraryFragment = LibraryFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filePickerLauncher = createFilePickerLauncher()

        bottomNav = findViewById(R.id.bottom_nav)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        bottomNav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_flash_cards -> ankiFragment
                R.id.nav_library -> libraryFragment
                else -> homeFragment
            }
            if (it.itemId != R.id.nav_add) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            } else {
                filePickerLauncher.launch("application/epub+zip")
            }
            true
        }
    }

    private fun createFilePickerLauncher(): ActivityResultLauncher<String> {
        return registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val fragment = UploadFragment()
                fragment.setBook(uri)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit()
            } else {
                Log.d("FileSelector", "No file selected")
            }
        }
    }
}