package com.devilishtruthstare.scribereader.ui.chemistry.flashcard

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File

class FlashCardDB(
    private val context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "flashcard.db"
        const val DATABASE_VERSION = 5

        @Volatile
        private var INSTANCE: FlashCardDB? = null
        fun getInstance(context: Context): FlashCardDB {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FlashCardDB(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getDataSets(context: Context): List<DataSet> {
            return getInstance(context).getDataSets()
        }
        const val TABLE_DATA_SET = "DataSet"
        const val COL_DATA_SET_ID = "DataSetId"
        const val COL_DATA_SET_NAME = "DataSetName"
        const val COL_DATA_SET_LOCATION = "DataSetLocation"
        const val COL_DATA_SET_VARIABLES = "DataSetVariables"
        const val COL_LAYOUT_FILE = "LayoutFile"
    }

    override fun onCreate(db: SQLiteDatabase) {
        createDataSetTable(db)
    }
    internal fun createDataSetTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_DATA_SET (
                $COL_DATA_SET_ID INTEGER PRIMARY KEY,
                $COL_DATA_SET_NAME TEXT NOT NULL,
                $COL_DATA_SET_LOCATION TEXT NOT NULL,
                $COL_DATA_SET_VARIABLES TEXT NOT NULL,
                $COL_LAYOUT_FILE TEXT NOT NULL
            );
        """
        )

        copyAssetToFile(context, "elements_layout.json")

        val contentValues = ContentValues().apply {
            put(COL_DATA_SET_NAME, "Periodic Table")
            put(COL_DATA_SET_LOCATION, "assets://elements_data.json")
            put(COL_DATA_SET_VARIABLES, "{TODO: state available variables here}")
            put(COL_LAYOUT_FILE, "elements_layout.json")
        }

        db.insert(TABLE_DATA_SET, null, contentValues)
    }

    // copy file assets://elements_layout.json to filesDir/layouts/elements_layout.json
    fun copyAssetToFile(context: Context, filename: String) {
        val inputStream = context.assets.open(filename)
        val outFile = File(context.filesDir, "layouts/$filename")
        outFile.parentFile?.mkdirs()

        inputStream.use { input ->
            outFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVewsion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DATA_SET")
        onCreate(db)
    }

    fun getDataSets(): List<DataSet> {
        return  readableDatabase.rawQuery("SELECT * FROM $TABLE_DATA_SET;", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(loadDataSet(cursor))
                }
            }
        }
    }
    internal fun loadDataSet(cursor: Cursor) = DataSet(
        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DATA_SET_ID)),
        name = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_SET_NAME)),
        fileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_SET_LOCATION)),
        variables = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_SET_VARIABLES)),
        layoutFileLocation = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAYOUT_FILE))
    )
}

data class DataSet(
    val id: Int = -1,
    val name: String = "",
    val fileLocation: String = "",
    val variables: String = "",
    val layoutFileLocation: String = ""
)



