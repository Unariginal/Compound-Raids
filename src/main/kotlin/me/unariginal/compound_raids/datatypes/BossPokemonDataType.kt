package me.unariginal.compound_raids.datatypes

class BossPokemonDataType {
    data class BossPokemon (
        val species: String,
        val form: String,
        val level: Int,
        val gender: String,
        val ivs: Values,
        val evs: Values,
        val moves: Moves,
        val scale: Double,
        val ability: String,
        val shiny: Boolean,
        val nature: String,
        val heldItem: String?,
        val nbtHeldItem: String?,
        val chance: Double,
        val fleeDistance: Int,
        val needRaidPass: Boolean?,
        val healthMultiplier: Double?
    )

    data class Values (
        val hp: Int,
        val def: Int,
        val sp_def: Int,
        val atk: Int,
        val sp_atk: Int,
        val spd: Int
    )

    data class Moves (
        val move1: String,
        val move2: String,
        val move3: String,
        val move4: String
    )
}