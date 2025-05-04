package com.devilishtruthstare.scribereader.dictionary

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Chapter
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.book.Token
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JMDict(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        @Volatile
        private var INSTANCE: JMDict? = null

        fun getInstance(context: Context): JMDict {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: JMDict(context.applicationContext).also { INSTANCE = it }
            }
        }

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

        private const val TABLE_BOOKS = "Books"
        private const val COL_BOOKS_ID = "BookId"
        private const val COL_BOOK_TITLE = "BookTitle"
        private const val CREATE_BOOKS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_BOOKS (
                $COL_BOOKS_ID INTEGER PRIMARY KEY,
                $COL_BOOK_TITLE TEXT NOT NULL
            )
        """

        private const val TABLE_CHAPTERS = "Chapters"
        private const val COL_CHAPTER_ID = "ChapterId"
        private const val COL_CHAPTER_POSITION = "ChapterPosition"
        private const val CREATE_CHAPTER_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_CHAPTERS (
                $COL_CHAPTER_ID INTEGER PRIMARY KEY,
                $COL_BOOKS_ID INTEGER NOT NULL,
                $COL_CHAPTER_POSITION INT NOT NULL
            )
        """

        private const val TABLE_PARAGRAPHS = "Paragraphs"
        private const val COL_PARAGRAPH_ID = "ParagraphId"
        private const val COL_PARAGRAPH_POSITION = "ParagraphPosition"
        private const val COL_IS_IMAGE = "Image"
        private const val COL_IMG_URL = "ImageUrl"
        private const val CREATE_PARAGRAPH_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_PARAGRAPHS (
                $COL_PARAGRAPH_ID INTEGER PRIMARY KEY,
                $COL_CHAPTER_ID INTEGER NOT NULL,
                $COL_PARAGRAPH_POSITION INTEGER NOT NULL,
                $COL_IS_IMAGE INTEGER NOT NULL,
                $COL_IMG_URL TEXT NOT NULL
            )
        """

        private const val TABLE_TOKENS = "Tokens"
        private const val COL_TOKEN_ID = "TokenId"
        private const val COL_DICTIONARY_FORM = "DictionaryForm"
        private const val COL_FEATURES = "Features"
        private const val COL_TOKEN_POSITION = "TokenPosition"
        private const val COL_TOKEN_SURFACE = "TokenSurface"
        private const val CREATE_TOKENS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_TOKENS (
                $COL_TOKEN_ID INTEGER PRIMARY KEY,
                $COL_PARAGRAPH_ID INTEGER NOT NULL,
                $COL_DICTIONARY_FORM TEXT NOT NULL,
                $COL_FEATURES TEXT NOT NULL,
                $COL_TOKEN_POSITION INTEGER NOT NULL,
                $COL_TOKEN_SURFACE TEXT NOT NULL
            )
        """

        private const val TABLE_EXAMPLE_SENTENCES = "ExampleSentences"
        private const val CREATE_EXAMPLE_SENTENCES_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_EXAMPLE_SENTENCES (
                $COL_TOKEN_ID INTEGER NOT NULL,
                $COL_ID INTEGER NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_ENTRY_TABLE_SQL)
        db.execSQL(CREATE_READING_TABLE_SQL)
        db.execSQL(CREATE_INDEX_READING)
        db.execSQL(CREATE_BOOKS_TABLE_SQL)
        db.execSQL(CREATE_CHAPTER_TABLE_SQL)
        db.execSQL(CREATE_PARAGRAPH_TABLE_SQL)
        db.execSQL(CREATE_TOKENS_TABLE_SQL)
        db.execSQL(CREATE_EXAMPLE_SENTENCES_TABLE_SQL)
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
            val db = this@JMDict.writableDatabase

            db.transaction {
                val gson = Gson()
                val insertEntryStmt =
                    db.compileStatement("INSERT INTO $ENTRY_TABLE ($COL_ID, $COL_JSON) VALUES (?, ?)")
                val insertReadingStmt =
                    db.compileStatement("INSERT INTO $READINGS_TABLE ($COL_ID, $COL_READING) VALUES (?, ?)")

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

    fun getEntries(text: String): List<Entry> {
        val gson = Gson()
        val querySQL = """
            SELECT * FROM $ENTRY_TABLE WHERE $COL_ID IN (SELECT DISTINCT $COL_ID FROM $READINGS_TABLE WHERE $COL_READING = ?);
        """
        return readableDatabase.rawQuery(querySQL, arrayOf(text)).use { cursor ->
            buildList {
                if (cursor.moveToFirst()) {
                    do {
                        val entryJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_JSON))
                        add(gson.fromJson(entryJson, Entry::class.java))
                    } while (cursor.moveToNext())
                }
            }
        }
    }

    suspend fun insertBook(
        book: Book,
        onTokenAdded: (() -> Unit),
        onFinished: (() -> Unit)) {
        withContext(Dispatchers.IO) {
            val db = this@JMDict.writableDatabase

            val uniqueForms = mutableSetOf<String>()
            book.chapters.forEach { chapter ->
                chapter.content.forEach { paragraph ->
                    paragraph.tokens.forEach { token ->
                        val tokenSearchForm = Token.getSearchTerm(token)
                        uniqueForms.add(tokenSearchForm)
                    }
                }
            }

            val tokenEntryMap = mutableMapOf<String, List<Entry>>()
            for (form in uniqueForms) {
                tokenEntryMap[form] = getEntries(form)
            }

            db.transaction {
                val bookValues = ContentValues().apply {
                    put(COL_BOOK_TITLE, book.title)
                }

                val bookId = db.insert(TABLE_BOOKS, null, bookValues)

                for ((c_i, chapter) in book.chapters.withIndex()) {
                    val chapterValues = ContentValues().apply {
                        put(COL_CHAPTER_POSITION, c_i)
                        put(COL_BOOKS_ID, bookId)
                    }
                    val chapterId = db.insert(TABLE_CHAPTERS, null, chapterValues)

                    for ((p_i, paragraph) in chapter.content.withIndex()) {
                        val paragraphValues = ContentValues().apply {
                            put(COL_PARAGRAPH_POSITION, p_i)
                            put(COL_CHAPTER_ID, chapterId)
                            put(COL_IS_IMAGE, paragraph.isImage)
                            put(COL_IMG_URL, if (paragraph.isImage) paragraph.content else "")
                        }
                        val paragraphId = db.insert(TABLE_PARAGRAPHS, null, paragraphValues)

                        for ((t_i, token) in paragraph.tokens.withIndex()) {
                            val tokenSearchForm = Token.getSearchTerm(token)
                            val tokenValues = ContentValues().apply {
                                put(COL_TOKEN_POSITION, t_i)
                                put(COL_FEATURES, token.features.toString())
                                put(COL_DICTIONARY_FORM, tokenSearchForm)
                                put(COL_PARAGRAPH_ID, paragraphId)
                                put(COL_TOKEN_SURFACE, token.surface)
                            }
                            val tokenId = db.insert(TABLE_TOKENS, null, tokenValues)

                            for (entry in tokenEntryMap[tokenSearchForm].orEmpty()) {
                                val exampleValues = ContentValues().apply {
                                    put(COL_TOKEN_ID, tokenId)
                                    put(COL_ID, entry.entSeq)
                                }
                                db.insert(TABLE_EXAMPLE_SENTENCES, null, exampleValues)
                            }
                        }
                        onTokenAdded()
                    }
                }
                onFinished()
            }
        }
    }
    fun getExampleSentences(entrySeq: Int): List<String> {
        val tokens = getExampleToken(entrySeq)
        val exampleSentences = mutableListOf<String>()
        var sentence = ""
        var currentParagraphId = tokens[0].paragraphId
        for (token in tokens) {
            if (token.paragraphId == currentParagraphId) {
                sentence += token.surface
            } else {
                exampleSentences.add(sentence)
                sentence = token.surface
                currentParagraphId = token.paragraphId
            }
        }
        if (sentence != "") {
            exampleSentences.add(sentence)
        }

        return exampleSentences.toList()
    }

    private fun getExampleToken(entrySeq: Int): List<TokenDto> {
        val query = """
            SELECT
                t.$COL_DICTIONARY_FORM,
                t.$COL_FEATURES,
                t.$COL_TOKEN_POSITION,
                t.$COL_PARAGRAPH_ID,
                t.$COL_TOKEN_SURFACE,
                p.$COL_PARAGRAPH_POSITION,
                c.$COL_CHAPTER_POSITION,
                b.$COL_BOOKS_ID
            FROM $TABLE_TOKENS t 
            JOIN $TABLE_PARAGRAPHS p ON t.$COL_PARAGRAPH_ID = p.$COL_PARAGRAPH_ID
            JOIN $TABLE_CHAPTERS c ON p.$COL_CHAPTER_ID = c.$COL_CHAPTER_ID
            JOIN $TABLE_BOOKS b ON c.$COL_BOOKS_ID = b.$COL_BOOKS_ID
            WHERE t.$COL_PARAGRAPH_ID IN (
                SELECT t2.$COL_PARAGRAPH_ID 
                FROM $TABLE_TOKENS t2
                WHERE t2.$COL_TOKEN_ID IN (
                    SELECT $COL_TOKEN_ID 
                    FROM $TABLE_EXAMPLE_SENTENCES
                    WHERE $COL_ID = ? LIMIT 100
                )
            )
            ORDER BY t.$COL_PARAGRAPH_ID, t.$COL_TOKEN_POSITION
        """
        return readableDatabase.rawQuery(query, arrayOf(entrySeq.toString())).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(TokenDto.build(cursor))
                }
            }
        }
    }

    fun getBookFromTokens(title: String): Book {
        val db = this.writableDatabase

        val query = """
            SELECT 
                $TABLE_TOKENS.$COL_DICTIONARY_FORM,
                $TABLE_TOKENS.$COL_FEATURES,
                $TABLE_TOKENS.$COL_TOKEN_POSITION,
                $TABLE_PARAGRAPHS.$COL_PARAGRAPH_POSITION,
                $TABLE_CHAPTERS.$COL_CHAPTER_POSITION,
                $TABLE_TOKENS.$COL_PARAGRAPH_ID,
                $TABLE_BOOKS.$COL_BOOKS_ID
            FROM $TABLE_TOKENS
            JOIN $TABLE_PARAGRAPHS ON $TABLE_TOKENS.$COL_PARAGRAPH_ID = $TABLE_PARAGRAPHS.$COL_PARAGRAPH_ID
            JOIN $TABLE_CHAPTERS ON $TABLE_PARAGRAPHS.$COL_CHAPTER_ID = $TABLE_CHAPTERS.$COL_CHAPTER_ID
            JOIN $TABLE_BOOKS ON $TABLE_CHAPTERS.$COL_BOOKS_ID = $TABLE_BOOKS.$COL_BOOKS_ID
            WHERE $TABLE_BOOKS.$COL_BOOK_TITLE = ?
            ORDER BY $TABLE_CHAPTERS.$COL_CHAPTER_POSITION, $TABLE_PARAGRAPHS.$COL_PARAGRAPH_POSITION, $TABLE_TOKENS.$COL_TOKEN_POSITION
        """

        val cursor = db.rawQuery(query, arrayOf(title))
        val tokens = buildList {
            cursor.use {
                while (it.moveToNext()) {
                    add(TokenDto.build(it))
                }
            }
        }

        // Get All Tokens from book
        var book = Book()
        var currentChapter = -1
        var currentParagraph = -1

        for (token in tokens) {
            if (currentChapter != token.chapterPosition) {
                book.chapters.add(Chapter())
                currentParagraph = 0
                currentChapter = token.chapterPosition
            }
            if (currentParagraph != token.paragraphPosition) {
                book.chapters[currentChapter].content.add(Content(isImage = false, content = "", tokens = mutableListOf(), imageResource = null, onPlaySoundClick = {}))
                currentParagraph = token.paragraphPosition
            }
            //book.chapters[currentChapter].content[currentParagraph].tokens.add(Token(surface = token.surface, features = token.features))
        }

        return book
    }

    data class TokenDto (
        val tokenPosition: Int,
        val paragraphId: Int,
        val dictionaryForm: String,
        val features: List<String>,
        val surface: String,

        val paragraphPosition: Int,
        val chapterPosition: Int,
        val bookId: Int
    ) {
        companion object {
            fun build(cursor: Cursor): TokenDto {
                return TokenDto(
                    tokenPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TOKEN_POSITION)),
                    paragraphPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PARAGRAPH_POSITION)),
                    chapterPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAPTER_POSITION)),
                    dictionaryForm = cursor.getString(cursor.getColumnIndexOrThrow(COL_DICTIONARY_FORM)),
                    features = Gson().fromJson(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FEATURES)),
                        object : TypeToken<List<String>>() {}.type
                    ),
                    surface = cursor.getString(cursor.getColumnIndexOrThrow(COL_TOKEN_SURFACE)),
                    paragraphId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PARAGRAPH_ID)),
                    bookId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKS_ID))
                )
            }
        }
    }
}