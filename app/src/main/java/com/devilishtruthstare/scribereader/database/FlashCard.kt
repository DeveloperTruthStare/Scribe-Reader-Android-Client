package com.devilishtruthstare.scribereader.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.devilishtruthstare.scribereader.jmdict.Entry

class FlashCard(
    private val context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "FlashCards.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
    fun addReading(frontOfCard: String, appearance: String, entries: List<Entry>) {
        Log.d("Flash Card", "Adding $frontOfCard to reading list")
    }
    fun addDefinition(frontOfCard: String, appearance: String, entries: List<Entry>) {
        Log.d("Flash Card", "Adding $frontOfCard to definition list")
    }
}
