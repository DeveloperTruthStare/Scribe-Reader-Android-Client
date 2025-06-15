package com.devilishtruthstare.scribereader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.ui.anki.AnkiFragment
import com.devilishtruthstare.scribereader.ui.home.HomeFragment
import com.devilishtruthstare.scribereader.ui.library.LibraryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val homeFragment: HomeFragment = HomeFragment()
    private val ankiFragment: AnkiFragment = AnkiFragment()
    private val libraryFragment: LibraryFragment = LibraryFragment()
    private var selectedItem: Int = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            selectedItem = it.itemId
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE) // No transition
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}