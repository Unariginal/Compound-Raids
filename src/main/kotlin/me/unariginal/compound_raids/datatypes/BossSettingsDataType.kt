package me.unariginal.compound_raids.datatypes

import com.cobblemon.mod.common.pokemon.Pokemon

class BossSettingsDataType {
    data class BossSettings (
        val fleeDistance: Int,
        val heldItem: String?,
        val needRaidPass: Boolean?,
        val healthMultiplier: Double,
        val started: Boolean
    )

    data class PokemonConfig (
        val chance: Double,
        val settings: BossSettings,
        val pokemon: Pokemon
    )
}