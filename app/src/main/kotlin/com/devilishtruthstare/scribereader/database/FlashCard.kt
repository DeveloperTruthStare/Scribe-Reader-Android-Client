package com.devilishtruthstare.scribereader.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FlashCard(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        @Volatile
        private var INSTANCE: FlashCard? = null
        fun getInstance(context: Context): FlashCard {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlashCard(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val DATABASE_NAME = "FlashCards.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_NAME = "FlashCards"
        private const val COL_ENTRY_SEQ = "EntSeq"
        private const val COL_STATUS = "Status"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_ENTRY_SEQ INTEGER NOT NULL,
                $COL_STATUS TEXT NOT NULL
            )
        """

        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
    fun addEntry(entSeq: Int, status: String) {
        if (hasEntry(entSeq)) {
            return
        }
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COL_ENTRY_SEQ, entSeq)
            put(COL_STATUS, status)
        }
        db.insert(TABLE_NAME, null, values)
    }
    fun getEntry(entSeq: Int): String {
        if (!hasEntry(entSeq)) { return "" }
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COL_ENTRY_SEQ, COL_STATUS),
            "$COL_ENTRY_SEQ = ?",
            arrayOf(entSeq.toString()),
            null, null, null
        )

        if (!cursor.moveToFirst()) {
            cursor.close()
            return ""
        }
        val status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))
        cursor.close()
        return status
    }
    fun hasEntry(entSeq: Int): Boolean {
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COL_ENTRY_SEQ),
            "$COL_ENTRY_SEQ = ?",
            arrayOf(entSeq.toString()),
            null, null, null
        )

        val hasEntry = cursor.moveToFirst()
        cursor.close()
        return hasEntry
    }
}
