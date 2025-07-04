package com.devilishtruthstare.scribereader.editor

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devilishtruthstare.scribereader.jmdict.Dictionary.Companion.DATABASE_NAME
import com.devilishtruthstare.scribereader.jmdict.Dictionary.Companion.DATABASE_VERSION

class FileManager(
    context: Context
) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    private val tables = mutableListOf("Files")
    private val TABLE_FILES = "Files"
    private val COL_FILE_LOCATION = "FileLocation"
    private val COL_FILE_NAME = "FileName"
    private val COL_FILE_TYPE = "FileType"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_FILES (
                $COL_FILE_NAME TEXT NOT NULL,
                $COL_FILE_LOCATION TEXT NOT NULL,
                $COL_FILE_TYPE TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        for (table in tables) {
            db.execSQL("DROP TABLE IF EXISTS $table")
        }
        onCreate(db)
    }

    fun addFile(fileDetails: FileDetails) {
        val contentValues = ContentValues().apply {
            put(COL_FILE_NAME, fileDetails.fileName)
            put(COL_FILE_LOCATION, fileDetails.fileLocation)
        }
        writableDatabase.insert(TABLE_FILES, null, contentValues)
    }

    fun getFileList(): List<FileDetails> {
        return get(TABLE_FILES) { cursor ->
            FileDetails(
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)),
                fileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_LOCATION)),
                fileType = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_TYPE))
            )
        }
    }

    fun <T> get(tableName: String, handleCursor: (cursor: Cursor) -> T): List<T> {
        val query = "SELECT * FROM $tableName"
        return readableDatabase.rawQuery(query, arrayOf()).use { cursor ->
            buildList {
                add(handleCursor(cursor))
            }
        }
    }
}

enum class FileType {
    MUSIC, JSON, OTHER
}

data class FileDetails (
    val fileName: String,
    val fileLocation: String,
    val fileType: String
)