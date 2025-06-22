package com.devilishtruthstare.scribereader.jmdict

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devilishtruthstare.scribereader.jmdict.Dictionary.Companion.COL_ENTRY_ID
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.devilishtruthstare.scribereader.jmdict.data.Tag

class TagDB(
    private val context: Context
) : SQLiteOpenHelper(context, Dictionary.DATABASE_NAME, null, Dictionary.DATABASE_VERSION) {
    companion object {
        enum class Result {
            SUCCESS, TAG_NOT_FOUND, DUPLICATE_ENTRY, NO_OP
        }
        @Volatile
        private var INSTANCE: TagDB? = null
        fun getInstance(context: Context): TagDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TagDB(context.applicationContext).also { INSTANCE = it }
            }
        }
        internal const val TABLE_TAG = "Tags"
        internal const val COL_TAG_ID = "TagId"
        internal const val COL_TAG_NAME = "TagName"
        private const val CREATE_TAG_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_TAG (
            $COL_TAG_ID INTEGER PRIMARY KEY,
            $COL_TAG_NAME TEXT NOT NULL
        );    
        """

        internal const val TABLE_TAG_DATA = "TagData"
        private const val CREATE_TAG_DATA_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_TAG_DATA (
                $COL_TAG_ID INTEGER NOT NULL,
                $COL_ENTRY_ID INTEGER NOT NULL
            );
        """
    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TAG_TABLE_SQL)
        db.execSQL(CREATE_TAG_DATA_TABLE_SQL)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAG")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TAG_DATA")
        onCreate(db)
    }

    fun getTags(): List<Tag> {
        val query = """
            SELECT * FROM $TABLE_TAG_DATA d INNER JOIN $TABLE_TAG t ON t.$COL_TAG_ID = d.$COL_TAG_ID;
        """

        val tagMap = mutableMapOf<String, Tag>()
        readableDatabase.rawQuery(query, arrayOf()).use { cursor ->
            buildList<Pair<String, Entry>> {
                if (cursor.moveToFirst()) {
                    val dict = Dictionary.getInstance(context)
                    do {
                        val tagName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TAG_NAME))
                        val entrySeq = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ENTRY_ID))
                        val entry = dict.get(entrySeq) ?: continue
                        tagMap.getOrPut(tagName) { Tag(tagName, mutableListOf()) }.entries.add(entry)
                    } while (cursor.moveToNext())
                }
            }
        }
        return tagMap.values.toList()
    }

    fun addTag(tagName: String): Result {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_TAG WHERE $COL_TAG_NAME = ?", arrayOf(tagName))
        cursor.use {
            if (it.moveToFirst()) return Result.DUPLICATE_ENTRY
        }

        val values = ContentValues().apply {
            put(COL_TAG_NAME, tagName)
        }
        db.insert(TABLE_TAG, null, values)
        return Result.SUCCESS
    }
    fun addToTag(tagName: String, entry: Int): Result {
        val db = writableDatabase
        val tagIdQuery = "SELECT $COL_TAG_ID FROM $TABLE_TAG WHERE $COL_TAG_NAME = ?"
        val tagId = db.rawQuery(tagIdQuery, arrayOf(tagName)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else return Result.TAG_NOT_FOUND
        }

        val existsQuery = """
            SELECT 1 FROM $TABLE_TAG_DATA WHERE $COL_TAG_ID = ? AND $COL_ENTRY_ID = ?
        """
        db.rawQuery(existsQuery, arrayOf(tagId.toString(), entry.toString())).use { cursor ->
            if (cursor.moveToFirst()) return Result.DUPLICATE_ENTRY
        }

        val values = ContentValues().apply {
            put(COL_TAG_ID, tagId)
            put(COL_ENTRY_ID, entry)
        }
        db.insert(TABLE_TAG_DATA, null, values)
        return Result.SUCCESS
    }
    fun removeFromTag(tagName: String, entrySeq: Int): Result {
        val db = writableDatabase
        val tagIdQuery = "SELECT $COL_TAG_ID FROM $TABLE_TAG WHERE $COL_TAG_NAME = ?"
        val tagId = db.rawQuery(tagIdQuery, arrayOf(tagName)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else return Result.TAG_NOT_FOUND
        }

        val deletedRows = db.delete(
            TABLE_TAG_DATA,
            "$COL_TAG_ID = ? AND $COL_ENTRY_ID = ?",
            arrayOf(tagId.toString(), entrySeq.toString())
        )
        return if (deletedRows > 0) Result.SUCCESS else Result.NO_OP
    }

    fun removeTag(tagName: String): Result {
        val db = writableDatabase
        val tagIdQuery = "SELECT $COL_TAG_ID FROM $TABLE_TAG WHERE $COL_TAG_NAME = ?"
        val tagId = db.rawQuery(tagIdQuery, arrayOf(tagName)).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else return Result.TAG_NOT_FOUND
        }

        db.delete(TABLE_TAG_DATA, "$COL_TAG_ID = ?", arrayOf(tagId.toString()))
        db.delete(TABLE_TAG, "$COL_TAG_ID = ?", arrayOf(tagId.toString()))
        return Result.SUCCESS
    }

}