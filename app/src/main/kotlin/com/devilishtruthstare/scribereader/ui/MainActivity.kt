package com.devilishtruthstare.scribereader.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.devilishtruthstare.scribereader.R
import com.devilishtruthstare.scribereader.ui.anki.AnkiFragment
import com.devilishtruthstare.scribereader.ui.home.HomeFragment
import com.devilishtruthstare.scribereader.ui.library.LibraryFragment
import com.devilishtruthstare.scribereader.ui.music.MusicFragment
import com.devilishtruthstare.scribereader.ui.tags.TagFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private val homeFragment = HomeFragment()
    private val tagFragment = TagFragment()
    private val musicFragment = MusicFragment()
    private val ankiFragment = AnkiFragment()
    private val libraryFragment = LibraryFragment()
    private var selectedItem = R.id.nav_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        bottomNav = findViewById(R.id.bottom_nav)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()

        bottomNav.setOnItemSelectedListener {
            val fragment = when (it.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_music -> musicFragment
                R.id.nav_flash_cards -> ankiFragment
                R.id.nav_library -> libraryFragment
                R.id.nav_tags -> tagFragment
                else -> homeFragment
            }
            selectedItem = it.itemId
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE) // No transition
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }
}