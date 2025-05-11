package com.devilishtruthstare.scribereader.mian

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.mian.anki.AnkiFragment
import com.devilishtruthstare.scribereader.mian.home.HomeFragment
import com.devilishtruthstare.scribereader.mian.library.LibraryFragment
import com.devilishtruthstare.scribereader.mian.uploader.UploadFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var filePickerLauncher: ActivityResultLauncher<String>
    private val homeFragment: HomeFragment = HomeFragment()
    private val ankiFragment: AnkiFragment = AnkiFragment()
    private val libraryFragment: LibraryFragment = LibraryFragment()
    private var selectedItem: Int = R.id.nav_home

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
                selectedItem = it.itemId
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) // No transition
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
                val uploadFragment = UploadFragment()
                uploadFragment.setBook(uri)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, uploadFragment)
                    .commit()
            } else {
                bottomNav.selectedItemId = selectedItem
            }
        }
    }
}