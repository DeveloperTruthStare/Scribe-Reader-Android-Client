package com.devilishtruthstare.scribereader.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class UserStats(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "Users.db"
        private const val DATABASE_VERSION = 3
        private const val STATS_TABLE = "UserStats"

        private const val COL_TITLE = "Title"
        private const val COL_CHAPTER = "Chapter"
        private const val COL_SECTION = "Section"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $STATS_TABLE (
            $COL_TITLE TEXT NOT NULL,
            $COL_CHAPTER INTEGER NOT NULL,
            $COL_SECTION INTEGER NOT NULL
        )
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $STATS_TABLE")
        onCreate(db)
    }

    fun getCurrentChapter(title: String): Int {
        val db = this.readableDatabase

        val cursor = db.query(
            STATS_TABLE,
            arrayOf(COL_TITLE, COL_CHAPTER),
            "$COL_TITLE = ?",
            arrayOf(title),
            null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return 0
        }
        val progress = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAPTER))
        cursor.close()
        return progress
    }

    fun getCurrentSection(title: String): Int {
        val db = this.readableDatabase

        val cursor = db.query(
            STATS_TABLE,
            arrayOf(COL_TITLE, COL_SECTION),
            "$COL_TITLE = ?",
            arrayOf(title),
            null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return 0
        }
        val progress = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTION))
        cursor.close()
        return progress
    }

    fun resetStats(title: String) {
        setCurrentPage(title, 0, 0)
    }

    fun setCurrentPage(title: String, chapter: Int, section: Int) {
        val db = this.writableDatabase

        // Check if an entry with the title already exists
        val cursor = db.query(
            STATS_TABLE,
            arrayOf(COL_TITLE),
            "$COL_TITLE = ?",
            arrayOf(title),
            null, null, null
        )

        // If entry exists, update it; otherwise, insert a new row
        if (cursor.moveToFirst()) {
            val values = ContentValues().apply {
                put(COL_CHAPTER, chapter)
                put(COL_SECTION, section)
            }
            db.update(STATS_TABLE, values, "$COL_TITLE = ?", arrayOf(title))
        } else {
            val values = ContentValues().apply {
                put(COL_TITLE, title)
                put(COL_CHAPTER, chapter)
                put(COL_SECTION, section)
            }
            db.insert(STATS_TABLE, null, values)
        }

        cursor.close()
    }
}