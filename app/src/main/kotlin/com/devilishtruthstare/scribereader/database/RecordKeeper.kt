package com.devilishtruthstare.scribereader.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devilishtruthstare.scribereader.book.Book

class RecordKeeper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        @Volatile
        private var INSTANCE: RecordKeeper? = null
        fun getInstance(context: Context): RecordKeeper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RecordKeeper(context.applicationContext).also { INSTANCE = it }
            }
        }
        private const val DATABASE_NAME = "DownloadedBooks.db"
        private const val DATABASE_VERSION = 3

        private const val TABLE_BOOKS = "DownloadedBooks"
        private const val COL_BOOK_ID = "BookId"
        private const val COL_TITLE = "Title"
        private const val COL_COVER = "Cover"
        private const val COL_FILE_LOCATION = "FileLocation"
        private const val COL_CHAPTER = "Chapter"
        private const val COL_SECTION = "Section"
        private const val COL_LAST_OPENED = "LastOpened"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_BOOKS (
                $COL_BOOK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_COVER TEXT NOT NULL,
                $COL_FILE_LOCATION TEXT NOT NULL,
                $COL_CHAPTER INT NOT NULL,
                $COL_SECTION INT NOT NULL,
                $COL_LAST_OPENED DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS")
        onCreate(db)
    }

    fun onBookDownloaded(book: Book) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_TITLE, book.title)
            put(COL_COVER, book.coverImage)
            put(COL_FILE_LOCATION, book.fileLocation)
            put(COL_CHAPTER, 0)
            put(COL_SECTION, 0)
        }
        db.insert(TABLE_BOOKS, null, contentValues)
    }

    fun onBookDeleted(book: Book) {
        val db = this.writableDatabase
        val whereClause = "$COL_TITLE = ?"
        db.delete(TABLE_BOOKS, whereClause, arrayOf(book.title))
    }
    fun hasBook(title: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BOOKS WHERE $COL_TITLE = ? LIMIT 1", arrayOf(title)
        )
        val foundBook = cursor.moveToFirst()
        cursor.close()
        return foundBook
    }
    fun getBook(title: String) : Book? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_BOOKS WHERE $COL_TITLE = ? LIMIT 1", arrayOf(title)
        )
        return cursor.use {
            it.takeIf { c -> c.moveToFirst() }?.let { loadBook(it) }
        }
    }
    fun getBookList() : List<Book> {
        val query = "SELECT * FROM $TABLE_BOOKS ORDER BY $COL_TITLE"
        return readableDatabase.rawQuery(query, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(loadBook(cursor))
                }
            }
        }
    }

    fun getBookListByRecency() : List<Book> {
        val query = "SELECT * FROM $TABLE_BOOKS ORDER BY $COL_LAST_OPENED DESC LIMIT 2"
        return readableDatabase.rawQuery(query, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(loadBook(cursor))
                }
            }
        }
    }

    fun getBookById(bookId: Int): Book? {
        val query = "SELECT * FROM $TABLE_BOOKS WHERE $COL_BOOK_ID = ? LIMIT 1"
        return readableDatabase.rawQuery(query, arrayOf(bookId.toString())).use {
            it.takeIf { c -> c.moveToFirst() }?.let { loadBook(it) }
        }
    }
    fun addOpenHistory(bookId: Int) {
        writableDatabase.execSQL(
            "UPDATE $TABLE_BOOKS SET $COL_LAST_OPENED = CURRENT_TIMESTAMP WHERE $COL_BOOK_ID = ?", arrayOf(bookId)
        )
    }
    fun setProgress(bookId: Int, currentChapter: Int, currentSection: Int) {
        writableDatabase.execSQL(
            "UPDATE $TABLE_BOOKS SET $COL_CHAPTER = ?, $COL_SECTION = ? WHERE $COL_BOOK_ID = ?", arrayOf(currentChapter, currentSection, bookId)
        )
    }

    private fun loadBook(cursor: Cursor) = Book().apply {
        bookId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_ID))
        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
        coverImage = cursor.getString(cursor.getColumnIndexOrThrow(COL_COVER))
        fileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_LOCATION))
        currentChapter = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAPTER))
        currentSection = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTION))
    }
}