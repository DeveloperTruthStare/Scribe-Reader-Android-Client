package com.devilishtruthstare.scribereader.jmdict

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JMDict(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "JMDict"
        private const val DATABASE_VERSION = 1

        private const val ENTRY_TABLE = "Entries"
        private const val COL_ID = "entSeq"
        private const val COL_JSON = "json"
        private const val CREATE_ENTRY_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $ENTRY_TABLE (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_JSON TEXT NOT NULL
            )
        """

        private const val READINGS_TABLE = "Readings"
        private const val COL_READING = "reading"
        private const val CREATE_READING_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $READINGS_TABLE (
                $COL_ID INTEGER NOT NULL,
                $COL_READING TEXT NOT NULL
            )
        """

        private const val CREATE_INDEX_READING = """
            CREATE INDEX idx_reading ON $READINGS_TABLE($COL_READING);
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_ENTRY_TABLE_SQL)
        db.execSQL(CREATE_READING_TABLE_SQL)
        db.execSQL(CREATE_INDEX_READING)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $ENTRY_TABLE")
        db.execSQL("DROP TABLE IF EXISTS $READINGS_TABLE")
        onCreate(db)
    }
    fun isDatabaseInitialized(): Boolean {
        val db = this.readableDatabase
        return try {
            // Try running a query that will fail if the table doesn't exist
            val cursor = db.rawQuery("SELECT COUNT(*) FROM $ENTRY_TABLE", null)
            cursor.use {
                it.moveToFirst()
                it.getInt(0) > 0
            }
        } catch (e: SQLiteException) {
            // Handle the case where the table doesn't exist
            false
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun loadEntries(entries: List<Entry>, onEntryAdded: (() -> Unit), onFinished: (() -> Unit)) {
        GlobalScope.launch(Dispatchers.Main) {
            // Call the background operation on IO dispatcher
            withContext(Dispatchers.IO) {

                val db = this@JMDict.writableDatabase

                db.beginTransaction()
                try {
                    entries.forEach { entry ->
                        // Get json of entry
                        val entryJson = Gson().toJson(entry)
                        val values = ContentValues().apply {
                            put(COL_ID, entry.entSeq)
                            put(COL_JSON, entryJson)
                        }
                        db.insert(ENTRY_TABLE, null, values)

                        entry.kana.forEach { kana ->
                            val kanaValue = ContentValues().apply {
                                put(COL_ID, entry.entSeq)
                                put(COL_READING, kana)
                            }
                            db.insert(READINGS_TABLE, null, kanaValue)
                        }
                        entry.kanji.forEach { kanji ->
                            val kanjiValue = ContentValues().apply {
                                put(COL_ID, entry.entSeq)
                                put(COL_READING, kanji)
                            }
                            db.insert(READINGS_TABLE, null, kanjiValue)
                        }
                        onEntryAdded()
                    }
                    db.setTransactionSuccessful()
                    onFinished()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }
    fun getEntries(text: String): List<Entry> {
        val db = this.readableDatabase
        val querySQL = """
            SELECT * FROM $ENTRY_TABLE WHERE $COL_ID IN (SELECT DISTINCT $COL_ID FROM $READINGS_TABLE WHERE $COL_READING = ?);
        """
        val cursor = db.rawQuery(querySQL, arrayOf(text))

        val list = mutableListOf<Entry>()

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val entryJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
                    list.add(Gson().fromJson(entryJson, Entry::class.java))
                } while(cursor.moveToNext())
            }
        }

        return list
    }
}