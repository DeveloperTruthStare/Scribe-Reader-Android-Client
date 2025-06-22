package com.devilishtruthstare.scribereader.jmdict

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.sqlite.transaction
import com.devilishtruthstare.scribereader.book.Book
import com.devilishtruthstare.scribereader.book.Chapter
import com.devilishtruthstare.scribereader.book.Content
import com.devilishtruthstare.scribereader.book.Token
import com.devilishtruthstare.scribereader.jmdict.data.Entry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryDB(
    private val context: Context
) : SQLiteOpenHelper(context, Dictionary.DATABASE_NAME, null, Dictionary.DATABASE_VERSION) {
    companion object {
        @Volatile
        private var INSTANCE: LibraryDB? = null
        fun getInstance(context: Context): LibraryDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LibraryDB(context.applicationContext).also { INSTANCE = it }
            }
        }

        private const val TABLE_BOOKS = "Books"
        private const val COL_BOOK_ID = "BookId"
        private const val COL_BOOK_TITLE = "BookTitle"
        private const val CREATE_BOOKS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_BOOKS (
                $COL_BOOK_ID INTEGER PRIMARY KEY,
                $COL_BOOK_TITLE TEXT NOT NULL
            )
        """

        private const val TABLE_CHAPTERS = "Chapters"
        private const val COL_CHAPTER_ID = "ChapterId"
        private const val COL_CHAPTER_POSITION = "ChapterPosition"
        private const val CREATE_CHAPTER_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_CHAPTERS (
                $COL_CHAPTER_ID INTEGER PRIMARY KEY,
                $COL_BOOK_ID INTEGER NOT NULL,
                $COL_CHAPTER_POSITION INT NOT NULL
            )
        """

        private const val TABLE_PARAGRAPHS = "Paragraphs"
        private const val COL_PARAGRAPH_ID = "ParagraphId"
        private const val COL_PARAGRAPH_POSITION = "ParagraphPosition"
        private const val COL_IS_IMAGE = "Image"
        private const val COL_PARAGRAPH_CONTENT = "Content"
        private const val CREATE_PARAGRAPH_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_PARAGRAPHS (
                $COL_PARAGRAPH_ID INTEGER PRIMARY KEY,
                $COL_CHAPTER_ID INTEGER NOT NULL,
                $COL_PARAGRAPH_POSITION INTEGER NOT NULL,
                $COL_IS_IMAGE INTEGER NOT NULL,
                $COL_PARAGRAPH_CONTENT TEXT NOT NULL
            )
        """

        private const val TABLE_TOKENS = "Tokens"
        private const val COL_TOKEN_ID = "TokenId"
        private const val COL_DICTIONARY_FORM = "DictionaryForm"
        private const val COL_FEATURES = "Features"
        private const val COL_TOKEN_POSITION = "TokenPosition"
        private const val COL_TOKEN_SURFACE = "TokenSurface"
        private const val COL_POS_PRIMARY = "PrimaryPartOfSpeech"
        private const val CREATE_TOKENS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_TOKENS (
                $COL_TOKEN_ID INTEGER PRIMARY KEY,
                $COL_PARAGRAPH_ID INTEGER NOT NULL,
                $COL_DICTIONARY_FORM TEXT NOT NULL,
                $COL_FEATURES TEXT NOT NULL,
                $COL_TOKEN_POSITION INTEGER NOT NULL,
                $COL_TOKEN_SURFACE TEXT NOT NULL,
                $COL_POS_PRIMARY TEXT NOT NULL
            )
        """

        private const val TABLE_EXAMPLE_SENTENCES = "ExampleSentences"
        private const val CREATE_EXAMPLE_SENTENCES_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS $TABLE_EXAMPLE_SENTENCES (
                $COL_TOKEN_ID INTEGER NOT NULL,
                ${Dictionary.COL_ENTRY_ID} INTEGER NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_BOOKS_TABLE_SQL)
        db.execSQL(CREATE_CHAPTER_TABLE_SQL)
        db.execSQL(CREATE_PARAGRAPH_TABLE_SQL)
        db.execSQL(CREATE_TOKENS_TABLE_SQL)
        db.execSQL(CREATE_EXAMPLE_SENTENCES_TABLE_SQL)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKS;")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAPTERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PARAGRAPHS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TOKENS")
        onCreate(db)
    }

    suspend fun insertBook(
        book: Book,
        onTokenAdded: (() -> Unit),
        onFinished: (() -> Unit)) {
        withContext(Dispatchers.IO) {
            val db = this@LibraryDB.writableDatabase

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
                tokenEntryMap[form] = Dictionary.getInstance(context).search(form)
            }

            db.transaction {
                val bookValues = ContentValues().apply {
                    put(COL_BOOK_TITLE, book.title)
                }

                val bookId = db.insert(TABLE_BOOKS, null, bookValues)

                for ((c_i, chapter) in book.chapters.withIndex()) {
                    val chapterValues = ContentValues().apply {
                        put(COL_CHAPTER_POSITION, c_i)
                        put(COL_BOOK_ID, bookId)
                    }
                    val chapterId = db.insert(TABLE_CHAPTERS, null, chapterValues)

                    for ((p_i, paragraph) in chapter.content.withIndex()) {
                        val paragraphValues = ContentValues().apply {
                            put(COL_PARAGRAPH_POSITION, p_i)
                            put(COL_CHAPTER_ID, chapterId)
                            put(COL_IS_IMAGE, paragraph.isImage)
                            put(COL_PARAGRAPH_CONTENT, paragraph.content)
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
                                put(COL_POS_PRIMARY, token.features[0])
                            }
                            val tokenId = db.insert(TABLE_TOKENS, null, tokenValues)

                            for (entry in tokenEntryMap[tokenSearchForm].orEmpty()) {
                                val exampleValues = ContentValues().apply {
                                    put(COL_TOKEN_ID, tokenId)
                                    put(Dictionary.COL_ENTRY_ID, entry.entSeq)
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

    fun getUniqueTokenList(bookId: Int): List<Triple<String, Int, String>> {
        val query = """
            WITH RankedTokens AS (
                SELECT
                    t.$COL_DICTIONARY_FORM,
                    t.$COL_TOKEN_POSITION,
                    p.$COL_PARAGRAPH_POSITION,
                    c.$COL_CHAPTER_POSITION,
                    t.$COL_POS_PRIMARY,
                    ROW_NUMBER() OVER (PARTITION BY t.$COL_DICTIONARY_FORM ORDER BY c.$COL_CHAPTER_POSITION, p.$COL_PARAGRAPH_POSITION, t.$COL_TOKEN_POSITION) AS rn,
                    COUNT(*) OVER (PARTITION BY t.$COL_DICTIONARY_FORM) AS token_count
                FROM
                    $TABLE_TOKENS t
                JOIN $TABLE_PARAGRAPHS p ON t.$COL_PARAGRAPH_ID = p.$COL_PARAGRAPH_ID
                JOIN $TABLE_CHAPTERS c ON p.$COL_CHAPTER_ID = c.$COL_CHAPTER_ID
                WHERE t.$COL_POS_PRIMARY NOT IN ('助詞', '記号', '感動詞', 'フィラー', '助動詞', '接続詞', 'その他') AND c.$COL_BOOK_ID = ?
            )
            SELECT
                $COL_DICTIONARY_FORM,
                $COL_TOKEN_POSITION,
                $COL_PARAGRAPH_POSITION,
                $COL_CHAPTER_POSITION,
                $COL_POS_PRIMARY,
                token_count
            FROM
                RankedTokens
            WHERE
                rn = 1
            ORDER BY
                $COL_POS_PRIMARY,
                token_count,
                $COL_CHAPTER_POSITION,
                $COL_PARAGRAPH_POSITION,
                $COL_TOKEN_POSITION;
        """
        return writableDatabase.rawQuery(query, arrayOf(bookId.toString())).use {
            buildList {
                while (it.moveToNext()) {
                    add(Triple<String, Int, String>(
                        it.getString(it.getColumnIndexOrThrow(COL_DICTIONARY_FORM)),
                        it.getInt(it.getColumnIndexOrThrow("token_count")),
                        it.getString(it.getColumnIndexOrThrow(COL_POS_PRIMARY))))
                }
            }
        }
    }

    fun getExampleSentences(entrySeq: Int): List<List<Token>> {
        val tokens = getExampleToken(entrySeq)
        val exampleSentences = mutableListOf<List<Token>>()
        var sentence = mutableListOf<Token>()
        var currentParagraphId = tokens[0].paragraphId
        for (token in tokens) {
            if (token.paragraphId == currentParagraphId) {
                sentence += Token(surface = token.surface, features =  token.features)
            } else {
                exampleSentences.add(sentence.toList())
                sentence = mutableListOf(Token(surface = token.surface, features = token.features))
                currentParagraphId = token.paragraphId
            }
        }
        if (sentence.isNotEmpty()) {
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
                b.$COL_BOOK_ID
            FROM $TABLE_TOKENS t 
            JOIN $TABLE_PARAGRAPHS p ON t.$COL_PARAGRAPH_ID = p.$COL_PARAGRAPH_ID
            JOIN $TABLE_CHAPTERS c ON p.$COL_CHAPTER_ID = c.$COL_CHAPTER_ID
            JOIN $TABLE_BOOKS b ON c.$COL_BOOK_ID = b.$COL_BOOK_ID
            WHERE t.$COL_PARAGRAPH_ID IN (
                SELECT t2.$COL_PARAGRAPH_ID 
                FROM $TABLE_TOKENS t2
                WHERE t2.$COL_TOKEN_ID IN (
                    SELECT $COL_TOKEN_ID 
                    FROM $TABLE_EXAMPLE_SENTENCES
                    WHERE $Dictionary.COL_ID = ? LIMIT 100
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

    private fun getContentForBook(title: String): List<ContentDto> {
        val db = this.readableDatabase

        val query = """
            SELECT
                p.$COL_PARAGRAPH_ID,
                p.$COL_PARAGRAPH_POSITION,
                p.$COL_PARAGRAPH_CONTENT,
                p.$COL_IS_IMAGE,
                c.$COL_CHAPTER_POSITION
            FROM $TABLE_PARAGRAPHS p
            INNER JOIN $TABLE_CHAPTERS c ON p.$COL_CHAPTER_ID = c.$COL_CHAPTER_ID
            INNER JOIN $TABLE_BOOKS b ON c.$COL_BOOK_ID = b.$COL_BOOK_ID
            WHERE b.$COL_BOOK_TITLE = ?
            ORDER BY c.$COL_CHAPTER_POSITION, p.$COL_PARAGRAPH_POSITION
        """

        val cursor = db.rawQuery(query, arrayOf(title))
        return buildList {
            cursor.use {
                while (it.moveToNext()) {
                    add(ContentDto.build(it))
                }
            }
        }
    }

    fun getBookFromTokens(title: String): Book {
        val db = this.writableDatabase

        val query = """
            SELECT 
                t.$COL_DICTIONARY_FORM,
                t.$COL_FEATURES,
                t.$COL_TOKEN_POSITION,
                t.$COL_TOKEN_SURFACE,
                p.$COL_PARAGRAPH_POSITION,
                c.$COL_CHAPTER_POSITION,
                t.$COL_PARAGRAPH_ID,
                b.$COL_BOOK_ID
            FROM $TABLE_TOKENS t
            JOIN $TABLE_PARAGRAPHS p ON t.$COL_PARAGRAPH_ID = p.$COL_PARAGRAPH_ID
            JOIN $TABLE_CHAPTERS c ON p.$COL_CHAPTER_ID = c.$COL_CHAPTER_ID
            JOIN $TABLE_BOOKS b ON c.$COL_BOOK_ID = b.$COL_BOOK_ID
            WHERE b.$COL_BOOK_TITLE = ?
            ORDER BY c.$COL_CHAPTER_POSITION, p.$COL_PARAGRAPH_POSITION, t.$COL_TOKEN_POSITION
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
        val book = Book()

        val content = getContentForBook(title)
        var currentChapter = -1

        for (paragraph in content) {
            if (currentChapter != paragraph.chapterPosition) {
                currentChapter++
                book.chapters.add(Chapter())
            }
            book.chapters[currentChapter].content.add(
                Content(
                isImage = paragraph.isImage,
                tokens = mutableListOf(),
                content = paragraph.content,
                isActive = false,
                imageResource = null
            ))
        }

        for (token in tokens) {
            if (book.chapters.size <= token.chapterPosition) {
                Log.e("PARSER", "not enough chapters")
            } else if (book.chapters[token.chapterPosition].content.size <= token.paragraphPosition) {
                Log.e("PARSER", "not enough paragraphs")
            } else {
                book.chapters[token.chapterPosition].content[token.paragraphPosition].tokens.add(Token(
                    surface = token.surface,
                    features = token.features
                ))
            }
        }

        return book
    }
    data class ContentDto(
        val isImage: Boolean,
        val content: String,
        val paragraphId: Int,
        val paragraphPosition: Int,
        val chapterPosition: Int
    ) {
        companion object {
            fun build(cursor: Cursor): ContentDto {
                return ContentDto(
                    isImage = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_IMAGE)) != 0,
                    content = cursor.getString(cursor.getColumnIndexOrThrow(COL_PARAGRAPH_CONTENT)),
                    paragraphPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PARAGRAPH_POSITION)),
                    paragraphId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PARAGRAPH_ID)),
                    chapterPosition = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CHAPTER_POSITION))
                )
            }
        }
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
                    bookId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOK_ID))
                )
            }
        }
    }


}