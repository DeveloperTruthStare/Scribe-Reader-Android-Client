package com.devilishtruthstare.scribereader.jmdict

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Dictionary(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val LEARNED_LEVEL = 5
        const val MAX_LEARNING_LEVEL = 3
        const val FIRST_VIEW_LEVEL = 0
        @Volatile
        private var INSTANCE: Dictionary? = null
        fun getInstance(context: Context): Dictionary {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Dictionary(context.applicationContext).also { INSTANCE = it }
            }
        }

        internal const val DATABASE_NAME = "JMDict"
        internal const val DATABASE_VERSION = 1

        internal const val ENTRY_TABLE = "Entries"
        internal const val COL_ENTRY_ID = "entSeq"
        internal const val COL_JSON = "json"
        internal const val COL_LEARNED = "learned"
        private const val CREATE_ENTRY_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $ENTRY_TABLE (
                $COL_ENTRY_ID INTEGER PRIMARY KEY,
                $COL_JSON TEXT NOT NULL,
                $COL_LEARNED INTEGER NOT NULL DEFAULT 0
            )
        """

        internal const val READINGS_TABLE = "Readings"
        internal const val COL_READING = "reading"
        private const val CREATE_READING_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $READINGS_TABLE (
                $COL_ENTRY_ID INTEGER NOT NULL,
                $COL_READING TEXT NOT NULL
            )
        """

        internal const val CREATE_INDEX_READING = """
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
        } catch (_: SQLiteException) {
            // Handle the case where the table doesn't exist
            false
        }
    }

    suspend fun loadEntries(
        entries: List<Entry>,
        onEntryAdded: (() -> Unit),
        onFinished: (() -> Unit)
    ) {
        withContext(Dispatchers.IO) {
            val db = this@Dictionary.writableDatabase

            db.transaction {
                val gson = Gson()
                val insertEntryStmt =
                    db.compileStatement("INSERT INTO $ENTRY_TABLE ($COL_ENTRY_ID, $COL_JSON) VALUES (?, ?)")
                val insertReadingStmt =
                    db.compileStatement("INSERT INTO $READINGS_TABLE ($COL_ENTRY_ID, $COL_READING) VALUES (?, ?)")

                entries.forEachIndexed { index, entry ->
                    val entryJson = gson.toJson(entry)

                    insertEntryStmt.bindLong(1, entry.entSeq.toLong())
                    insertEntryStmt.bindString(2, entryJson)
                    insertEntryStmt.executeInsert()
                    insertEntryStmt.clearBindings()

                    entry.kana.forEach { kana ->
                        insertReadingStmt.bindLong(1, entry.entSeq.toLong())
                        insertReadingStmt.bindString(2, kana)
                        insertReadingStmt.executeInsert()
                        insertReadingStmt.clearBindings()
                    }

                    entry.kanji.forEach { kanji ->
                        insertReadingStmt.bindLong(1, entry.entSeq.toLong())
                        insertReadingStmt.bindString(2, kanji)
                        insertReadingStmt.executeInsert()
                        insertReadingStmt.clearBindings()
                    }

                    if (index % 100 == 0) onEntryAdded()
                }
                onFinished()
            }
        }
    }
    fun get(entSeq: Int): Entry? {
        val query = "SELECT * FROM $ENTRY_TABLE WHERE $COL_ENTRY_ID = ?;"
        val gson = Gson()
        return readableDatabase.rawQuery(query, arrayOf(entSeq.toString())).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val entryJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
            val entry = gson.fromJson(entryJson, Entry::class.java)
            entry.level = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LEARNED))
            entry
        }
    }
    fun search(text: String): List<Entry> {
        val gson = Gson()
        val querySQL = """
            SELECT * FROM $ENTRY_TABLE WHERE $COL_ENTRY_ID IN (SELECT DISTINCT $COL_ENTRY_ID FROM $READINGS_TABLE WHERE $COL_READING = ?);
        """
        return readableDatabase.rawQuery(querySQL, arrayOf(text)).use { cursor ->
            buildList {
                if (cursor.moveToFirst()) {
                    do {
                        val entryJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
                        val entry = gson.fromJson(entryJson, Entry::class.java)
                        entry.level = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LEARNED))
                        add(entry)
                    } while (cursor.moveToNext())
                }
            }
        }
    }

    fun markAsLearned(searchTerm: String) {
        val entries = search(searchTerm)
        setEntryLevels(entries) { LEARNED_LEVEL }
    }
    fun setEntryLevels(entries: List<Entry>, getNewLevel: (it: Entry) -> Int) {
        for(entry in entries) {
            val newLevel = getNewLevel(entry)
            setEntryLevel(entry.entSeq, newLevel)
        }
    }
    fun setEntryLevel(entSeq: Int, level: Int) {
        writableDatabase.execSQL(
            "UPDATE $ENTRY_TABLE SET $COL_LEARNED = ? WHERE $COL_ENTRY_ID = ?", arrayOf(level, entSeq)
        )
    }
}