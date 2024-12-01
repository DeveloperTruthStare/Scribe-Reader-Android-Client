package com.devilishtruthstare.scribereader.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.devilishtruthstare.scribereader.book.Book

class RecordKeeper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "DownloadedBooks.db"
        private const val DATABASE_VERSION = 3
        private const val TABLE_NAME = "DownloadedBooks"
        private const val COL_TITLE = "Title"
        private const val COL_COVER = "Cover"
        private const val COL_FILE_LOCATION = "FileLocation"
    }

    private val userStats = UserStats(context)
    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                $COL_TITLE TEXT NOT NULL,
                $COL_COVER TEXT NOT NULL,
                $COL_FILE_LOCATION TEXT NOT NULL
            )
        """
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun onBookDownloaded(book: Book) {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_TITLE, book.title)
        contentValues.put(COL_COVER, book.coverImage)
        contentValues.put(COL_FILE_LOCATION, book.fileLocation)
        db.insert(TABLE_NAME, null, contentValues)
    }
    fun onBookDeleted(book: Book) {
        val db = this.writableDatabase
        val whereClause = "$COL_TITLE = ?"
        db.delete(TABLE_NAME, whereClause, arrayOf(book.title))
    }

    fun hasBook(title: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_TITLE = ? LIMIT 1", arrayOf(title)
        )
        val foundBook = cursor.moveToFirst()
        cursor.close()
        return foundBook
    }
    fun getBook(title: String) : Book? {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_NAME WHERE $COL_TITLE = ? LIMIT 1", arrayOf(title)
        )
        var book : Book? = null
        if (cursor.moveToFirst()) {
            book = loadBook(cursor)
        }
        cursor.close()
        return book
    }

    fun getBookList() : List<Book> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COL_TITLE", null)

        val books = mutableListOf<Book>()

        if (cursor.moveToFirst()) {
            do {
                books.add(loadBook(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()

        return books
    }

    private fun loadBook(cursor: Cursor): Book {
        val book = Book()
        book.title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
        book.coverImage = cursor.getString(cursor.getColumnIndexOrThrow(COL_COVER))
        book.fileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_LOCATION))
        book.currentChapter = userStats.getCurrentChapter(book.title)
        book.currentSection = userStats.getCurrentSection(book.title)
        return book
    }
}