package com.devilishtruthstare.scribereader.pokemon

enum class PokemonGames {
    Red, Blue, Green, Yellow,
    Gold, Silver, Crystal,
    Ruby, Sapphire, Emerald, FireRed, LeafGreen,
    Diamond, Pearl, Platinum, HeartGold, SoulSilver,
    Black, White, Black2, White2,
    X, Y, OmegaRuby, AlphaSapphire,
    Sun, Moon, UltraSun, UltraMoon, LetsGoEevee, LetsGoPikachu,
    Sword, Shield, BrilliantDiamond, ShiningPearl, LegendsArceus,
    Scarlet, Violet, LegendsZA,

    PokemonGo, Colosseum, XDGaleOfDarkness
}

enum class TypeNames {
    NORMAL, FIRE, WATER, ELECTRIC, GRASS, ICE, FIGHTING, POISON, GROUND, FLYING, PSYCHIC, BUG, ROCK, GHOST, DRAGON, DARK, STEEL, FAIRY
}

enum class Regions {
    Kanto, Jhoto, Hoenn, Sinnoh, Hisui, Unova, Kalos, Alola, Galar, Paldea
}
/*
val xdd = {
    "regions": [
    {
        "name": "Kanto",
        "sub-regions": [
        {
            "name": "Generation I",
            "sub-regions": [
            { "name": "Red" },
            { "name": "Blue" },
            { "name": "Green" },
            { "name": "Yellow" }
            ]
        },
        {
            "name": "Generation II",
            "sub-regions": [
            { "name": "Gold" },
            { "name": "Silver" },
            { "name": "Crystal" }
            ]
        },
        {
            "name": "Generation III",
            "sub-regions": [
            { "name": "FireRed" },
            { "name": "LeafGreen" }
            ]
        },
        {
            "name": "Generation IV",
            "sub-regions": [
            { "name": "HeartGold" },
            { "name": "SoulSilver" }
            ]
        },
        {
            "name": "Generation VII",
            "sub-regions": [
            { "name": "Let's Go Eevee" },
            { "name": "Let's Go Pikachu" }
            ]
        },
        ]
    }
    {
        "name": "Jhoto",
        "sub-regions": [
        {
            "name": "Generation II",
            "sub-regions": [
            { "name": "Gold" },
            { "name": "Silver" },
            { "name": "Crystal" }
            ]
        },
        {
            "name": "Generation IV",
            "sub-regions": [
            { "name": "HeartGold" },
            { "name": "SoulSilver" }
            ]
        },
        ]
    },
    {
        "name": "Hoenn",
        "sub-regions": [
        {
            "name": "Generation III",
            "sub-regions": [
            { "name": "Ruby" },
            { "name": "Sapphire" },
            { "name": "Emerald" }
            ]
        },
        {
            "name": "Generation XI",
            "sub-regions": [
            { "name": "OmegaRuby" },
            { "name": "AlphaSapphire" }
            ]
        }
        ]
    },
    {
        "name": "Sinnoh",
        "sub-regions": [
        {
            "name": "Generation IV" ,
            "sub-regions": [
            { "name": "Diamond" },
            { "name": "Pearl" },
            { "name": "Platinum" }
            ]
        },
        {
            "name": "Generation IIX" ,
            "sub-regions": [
            { "name": "BrilliantDiamond" },
            { "name": "ShinningPearl" }
            ]
        }
        ]
    },
    {
        "name": "Unova",
        "sub-regions": [
            {
                "name": "Generation V",
                "sub-regions": [
                    { "name": "Black" },
                    { "name": "White" },
                    { "name": "Black 2" },
                    { "name": "White 2" }
                ]
            }
        ]
    },
    {
        "name": "Kalos",
        "sub-regions": [
        {
            "name": "Generation VI",
            "sub-regions": [
            { "name": "X" },
            { "name": "Y" },
            ]
        },
        {
            "name": "Generation IX",
            "sub-regions": [
            { "name": "Legends Z-A" }
            ]
        }
        ]
    },
    {
        "name": 
    }
    ]
}
*/

val pokedexes = mutableMapOf<Regions, MutableList<String>>()
fun init() {
    pokedexes[Regions.Kanto] = mutableListOf("Generation I", "Generation III", "Generation VII")
    pokedexes[Regions.Jhoto] = mutableListOf("Generation II", "Generation IV")
    pokedexes[Regions.Hoenn] = mutableListOf("Generation III", "Generation VI")
    pokedexes[Regions.Sinnoh] = mutableListOf("Generation IV", "Generation IIX")
    pokedexes[Regions.Hisui] = mutableListOf()
    pokedexes[Regions.Unova] = mutableListOf("Generation V", "Generation IX")
    pokedexes[Regions.Kalos] = mutableListOf("X", "Y", "Legends Z-A")
}

val Normal = TypeNames.NORMAL
val Fire = TypeNames.FIRE
val Water = TypeNames.WATER
val Electric = TypeNames.ELECTRIC
val Grass = TypeNames.GRASS
val Ice = TypeNames.ICE
val Fighting = TypeNames.FIGHTING
val Poison = TypeNames.POISON
val Ground = TypeNames.GROUND
val Flying = TypeNames.FLYING
val Psychic = TypeNames.PSYCHIC
val Bug = TypeNames.BUG
val Rock = TypeNames.ROCK
val Ghost = TypeNames.GHOST
val Dragon = TypeNames.DRAGON
val Dark = TypeNames.DARK
val Steel = TypeNames.STEEL
val Fairy = TypeNames.FAIRY



class Type {
    var Name: String = ""
    var superEffectiveAgainst = mutableListOf<String>()
    var weakTo = mutableListOf<String>()
}

val types = setOf("Normal", "Fire", "Water", "Electric", "Grass", "Ice", "Fighting", "Poison", "Ground", "Flying", "Psychic", "Bug", "Rock", "Ghost", "Dragon", "Dark", "Steel", "Fairy")

val NUM_TYPES = 18

val half = setOf(
    setOf(Normal, Rock),
    setOf(Normal, Steel),

    setOf(Fire, Fire),
    setOf(Fire, Water),
    setOf(Fire, Rock),
    setOf(Fire, Dragon),

    setOf(Water, Water),
    setOf(Water, Grass),
    setOf(Water, Dragon),

    setOf(Electric, Electric),
    setOf(Electric, Grass),
    setOf(Electric, Dragon),

    setOf(Grass, Fire),
    setOf(Grass, Grass),
    setOf(Grass, Poison),
    setOf(Grass, Flying),
    setOf(Grass, Bug),
    setOf(Grass, Dragon),
    setOf(Grass, Steel),

    setOf(Ice, Fire),
    setOf(Ice, Water),
    setOf(Ice, Ice),
    setOf(Ice, Steel),

    setOf(Fighting, Poison),
    setOf(Fighting, Flying),
    setOf(Fighting, Psychic),
    setOf(Fighting, Bug),
    setOf(Fighting, Fairy),

    setOf(Poison, Poison),
    setOf(Poison, Ground),
    setOf(Poison, Rock),
    setOf(Poison, Ghost),

    setOf(Ground, Grass),
    setOf(Ground, Bug),

    setOf(Flying, Electric),
    setOf(Flying, Rock),
    setOf(Flying, Steel),

    setOf(Psychic, Psychic),
    setOf(Psychic, Steel),

    setOf(Bug, Fire),
    setOf(Bug, Fighting),
    setOf(Bug, Poison),
    setOf(Bug, Flying),
    setOf(Bug, Ghost),
    setOf(Bug, Steel),
    setOf(Bug, Fairy),

    setOf(Rock, Fighting),
    setOf(Rock, Ground),
    setOf(Rock, Steel),

    setOf(Ghost, Dark),

    setOf(Dragon, Steel),

    setOf(Dark, Fighting),
    setOf(Dark, Dark),
    setOf(Dark, Fairy),

    setOf(Steel, Fire),
    setOf(Steel, Water),
    setOf(Steel, Electric),
    setOf(Steel, Steel),

    setOf(Fairy, Fire),
    setOf(Fairy, Poison),
    setOf(Fairy, Steel)
)

val double = setOf(
    setOf(Fire, Grass),
    setOf(Fire, Ice),
    setOf(Fire, Bug),
    setOf(Fire, Steel),

    setOf(Water, Fire),
    setOf(Water, Ground),
    setOf(Water, Rock),

    setOf(Electric, Water),
    setOf(Electric, Flying),

    setOf(Grass, Water),
    setOf(Grass, Ground),
    setOf(Grass, Rock),

    setOf(Ice, Grass),
    setOf(Ice, Ground),
    setOf(Ice, Flying),
    setOf(Ice, Dragon),

    setOf(Fighting, Normal),
    setOf(Fighting, Ice),
    setOf(Fighting, Rock),
    setOf(Fighting, Dark),
    setOf(Fighting, Steel),

    setOf(Poison, Grass),
    setOf(Poison, Fairy),

    setOf(Ground, Fire),
    setOf(Ground, Electric),
    setOf(Ground, Poison),
    setOf(Ground, Rock),
    setOf(Ground, Steel),

    setOf(Flying, Grass),
    setOf(Flying, Fighting),
    setOf(Flying, Bug),

    setOf(Psychic, Fighting),
    setOf(Psychic, Poison),

    setOf(Bug, Grass),
    setOf(Bug, Psychic),
    setOf(Bug, Dark),

    setOf(Rock, Fire),
    setOf(Rock, Ice),
    setOf(Rock, Flying),
    setOf(Rock, Bug),

    setOf(Ghost, Psychic),
    setOf(Ghost, Ghost),

    setOf(Dragon, Dragon),

    setOf(Dark, Psychic),
    setOf(Dark, Ghost),

    setOf(Steel, Ice),
    setOf(Steel, Rock),
    setOf(Steel, Fairy),

    setOf(Fairy, Fighting),
    setOf(Fairy, Dragon),
    setOf(Fairy, Dark)
)

val immune = setOf(
    setOf(Normal, Ghost),
    setOf(Electric, Ground),
    setOf(Fighting, Ghost),
    setOf(Poison, Steel),
    setOf(Ground, Flying),
    setOf(Psychic, Dark),
    setOf(Ghost, Normal),
    setOf(Dragon, Fairy),
)

var typeChart = setOf(
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0.5, 0, 1, 1, 0.5, 1),
    setOf(1, 0.5, 0.5, 1, 2, 2, 1, 1, 1, 1, 1, 2, 0.5, 1, 0.5, 1, 2, 1),
    setOf(1, 0.5, 0.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    setOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
)

/*

// Show By Region
Kanto
- Gen I
- Gen III
- Gen VII

Johto
- Gen II
- Gen IV
- Gen X

Hoenn
- Gen III
- Gen VI

Sinnoh
- DP
- Pt

Hisui

Unova
- Black/White
- Black 2/White 2

Kalos
- X/Y
- - Kalos Central
- - Kalos Coastal
- - Kalos Mountain
- Legends Z-A

Alola
- Sun/Moon
- - Melemele
- - Akala
- - Ulaula
- - Poni

- Ultra Sun/Ultra Moon
- - Melemele
- - Akala
- - Ulaula
- - Poni

Galar
- Sword/Shield
- - Base
- - Isle of Armor
- - Crown Tundra

Paldea
- Scarlet/Violet
- - Base
- - Kitakami
- - Blueberry

 */