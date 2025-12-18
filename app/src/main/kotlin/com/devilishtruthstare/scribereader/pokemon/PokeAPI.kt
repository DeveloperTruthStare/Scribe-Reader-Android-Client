package com.devilishtruthstare.scribereader.pokemon

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream

class PokeAPI(private val context: Context):
SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "pokeapi.db"
        private const val DB_VERSION = 1
    }

    override fun onCreate(p0: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}


    fun getListOfPokemon(): List<PokemonDBO> {
        val query = """
        SELECT 
            p.id, 
            p.identifier AS name, 
            p.species_id,
            t1.identifier AS type1,
            t1.id as type1Id,
            t2.identifier AS type2,
            t2.id as type2Id
        FROM pokemon AS p
        LEFT JOIN pokemon_types AS pt1 
            ON pt1.pokemon_id = p.id AND pt1.slot = 1
        LEFT JOIN types AS t1 
            ON t1.id = pt1.type_id
        LEFT JOIN pokemon_types AS pt2 
            ON pt2.pokemon_id = p.id AND pt2.slot = 2
        LEFT JOIN types AS t2 
            ON t2.id = pt2.type_id
        ORDER BY p.id
    """.trimIndent()

        readableDatabase.rawQuery(query, null).use { cursor ->
            return buildList {
                while (cursor.moveToNext()) {
                    add(
                        PokemonDBO(
                            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            speciesId = cursor.getInt(cursor.getColumnIndexOrThrow("species_id")),
                            type1 = cursor.getInt(cursor.getColumnIndexOrThrow("type1Id")),
                            type2 = cursor.getIntOrNull(cursor.getColumnIndexOrThrow("type2Id"))
                        )
                    )
                }
            }
        }
    }

    fun getPokemon(pokemonId: Int): PokemonDBO? {
        val query = """
        SELECT 
            p.id, 
            p.identifier AS name, 
            p.species_id,
            t1.identifier AS type1,
            t1.id as type1Id,
            t2.identifier AS type2,
            t2.id as type2Id
        FROM pokemon AS p
        LEFT JOIN pokemon_types AS pt1 
            ON pt1.pokemon_id = p.id AND pt1.slot = 1
        LEFT JOIN types AS t1 
            ON t1.id = pt1.type_id
        LEFT JOIN pokemon_types AS pt2 
            ON pt2.pokemon_id = p.id AND pt2.slot = 2
        LEFT JOIN types AS t2 
            ON t2.id = pt2.type_id
        WHERE p.id = ?
    """.trimIndent()

        readableDatabase.rawQuery(query, arrayOf(pokemonId.toString())).use { cursor ->
            return if (cursor.moveToFirst()) {
                PokemonDBO(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    speciesId = cursor.getInt(cursor.getColumnIndexOrThrow("species_id")),
                    type1 = cursor.getInt(cursor.getColumnIndexOrThrow("type1Id")),
                    type2 = cursor.getIntOrNull(cursor.getColumnIndexOrThrow("type2Id"))
                )
            } else null
        }
    }

    // Helper to safely handle nullable text columns
    private fun Cursor.getStringOrNull(columnIndex: Int): String? =
        if (isNull(columnIndex)) null else getString(columnIndex)
    private fun Cursor.getIntOrNull(columnIndex: Int): Int? =
        if (isNull(columnIndex)) null else getInt(columnIndex)


    fun copyDatabaseFromAssets() {
        val dbPath = context.getDatabasePath(DB_NAME)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

data class PokemonDBO (
    val id: Int,
    val name: String,
    val speciesId: Int,
    val type1: Int,
    val type2: Int?
)