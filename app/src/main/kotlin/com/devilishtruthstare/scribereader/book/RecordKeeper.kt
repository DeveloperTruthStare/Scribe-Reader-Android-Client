package com.devilishtruthstare.scribereader.book

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

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
        private const val DATABASE_VERSION = 1

        private const val TABLE_BOOKS = "DownloadedBooks"
        private const val COL_BOOK_ID = "BookId"
        private const val COL_TITLE = "Title"
        private const val COL_COVER = "Cover"
        private const val COL_FILE_LOCATION = "FileLocation"
        private const val COL_CHAPTER = "Chapter"
        private const val COL_SECTION = "Section"
        private const val COL_LAST_OPENED = "LastOpened"
        private const val COL_AUTHOR = "Author"
        private const val COL_LANGUAGE = "Language"
        private const val COL_STATUS = "Status"
        private const val COL_PREFER_VERTICAL_TEXT = "VerticalText"
        private const val COL_LEARNING_MODE = "LearningMode"

        const val STATUS_NOT_STARTED = "NOT_STARTED"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_FINISHED = "FINISHED"
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
                $COL_AUTHOR TEXT NOT NULL,
                $COL_LANGUAGE TEXT NOT NULL,
                $COL_STATUS TEXT NOT NULL,
                $COL_PREFER_VERTICAL_TEXT TEXT DEFAULT "${Book.VERTICAL_TEXT_DEFAULT}",
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
            put(COL_AUTHOR, book.author)
            put(COL_STATUS, STATUS_NOT_STARTED)
            put(COL_LANGUAGE, book.language)
        }
        db.insert(TABLE_BOOKS, null, contentValues)
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
        val query = "SELECT * FROM $TABLE_BOOKS ORDER BY $COL_AUTHOR, $COL_TITLE"
        return readableDatabase.rawQuery(query, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(loadBook(cursor))
                }
            }
        }
    }

    fun getBookListByRecency() : List<Book> {
        val query = "SELECT * FROM $TABLE_BOOKS ORDER BY $COL_LAST_OPENED DESC LIMIT 3"
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
    fun startBook(bookId: Int) {
        setStatus(bookId, STATUS_IN_PROGRESS)
    }
    fun finishBook(bookId: Int) {
        setStatus(bookId, STATUS_FINISHED)
    }
    private fun setStatus(bookId: Int, status: String) {
        writableDatabase.execSQL(
            "UPDATE $TABLE_BOOKS SET $COL_STATUS = ? WHERE $COL_BOOK_ID = ?", arrayOf(status, bookId.toString())
        )
    }

    private fun loadBook(cursor: Cursor) = Book().apply {
        bookId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_ID))
        title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
        coverImage = cursor.getString(cursor.getColumnIndexOrThrow(COL_COVER))
        fileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_LOCATION))
        currentChapter = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAPTER))
        currentSection = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SECTION))
        status = cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS))
        author = cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR))
        textMode = cursor.getString(cursor.getColumnIndexOrThrow(
            COL_PREFER_VERTICAL_TEXT
        ))
    }
}