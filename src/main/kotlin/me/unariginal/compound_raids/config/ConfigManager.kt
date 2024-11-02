package me.unariginal.compound_raids.config

import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.api.moves.Moves
import com.cobblemon.mod.common.api.pokemon.Natures
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies
import com.cobblemon.mod.common.api.pokemon.stats.Stats
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import me.unariginal.compound_raids.CompoundRaids
import me.unariginal.compound_raids.CompoundRaids.LOGGER
import me.unariginal.compound_raids.CompoundRaids.bossSettings
import me.unariginal.compound_raids.datatypes.BossPokemonDataType
import me.unariginal.compound_raids.datatypes.BossSettingsDataType
import net.minecraft.item.Item
import net.minecraft.nbt.StringNbtReader
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import java.io.File

class ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dirBosses = File("config/compoundraids/bosses")
    //private val dirCache = File("config/compoundraids/cache")
    private val fileQueue = File("config/compoundraids/queue.json")
    //private val fileCurrentUUIDs = File("config/compoundraids/cache/currentBossUUIDs.json")
    //private val fileLeaderboards = File("config/compoundraids/cache/leaderboards.json")

    init {
        if (!dirBosses.exists()) {dirBosses.mkdirs()}
        //if (!dirCache.exists()) {dirCache.mkdirs()}
        if (!fileQueue.exists()) {fileQueue.createNewFile()}
        //if (!fileCurrentUUIDs.exists()) {fileCurrentUUIDs.createNewFile()}
        //if (!fileLeaderboards.exists()) {fileLeaderboards.createNewFile()}

        //load()
    }

    fun selectBoss(): Pokemon? {
        val loadedBosses = mutableMapOf<Pokemon, BossSettingsDataType.PokemonConfig>()

        val preference = queueEmpty()

        dirBosses.listFiles { _, name -> name.contains("$preference.json")}?.forEach { file ->
            val json = file.readText()
            if (json.isNotEmpty()) {
                val jsonObject = gson.fromJson(json, BossPokemonDataType.BossPokemon::class.java)

                val boss = PokemonSpecies.getByName(jsonObject.species.lowercase())!!.create(jsonObject.level)
                boss.features.clear()
                PokemonProperties.parse(jsonObject.form).apply(boss)
                boss.scaleModifier = jsonObject.scale.toFloat()

                when (jsonObject.gender.lowercase()) {
                    "male" -> boss.gender = Gender.MALE
                    "female" -> boss.gender = Gender.FEMALE
                    "genderless" -> boss.gender = Gender.GENDERLESS
                }

                boss.updateAbility(AbilityTemplate(jsonObject.ability).create())
                boss.shiny = jsonObject.shiny
                boss.nature = Natures.getNature(jsonObject.nature)!!

                if (jsonObject.heldItem != null && jsonObject.heldItem != "") {
                    val item = getItem(jsonObject.heldItem).defaultStack
                    if (jsonObject.nbtHeldItem != null && jsonObject.nbtHeldItem != "") {
                        item.nbt = StringNbtReader.parse(jsonObject.nbtHeldItem)
                    }
                    boss.swapHeldItem(item)
                }

                boss.moveSet.setMove(0, Moves.getByName(jsonObject.moves.move1)!!.create())
                boss.moveSet.setMove(1, Moves.getByName(jsonObject.moves.move2)!!.create())
                boss.moveSet.setMove(2, Moves.getByName(jsonObject.moves.move3)!!.create())
                boss.moveSet.setMove(3, Moves.getByName(jsonObject.moves.move4)!!.create())

                boss.setEV(Stats.HP, jsonObject.evs.hp)
                boss.setEV(Stats.DEFENCE, jsonObject.evs.def)
                boss.setEV(Stats.SPECIAL_DEFENCE, jsonObject.evs.sp_def)
                boss.setEV(Stats.ATTACK, jsonObject.evs.atk)
                boss.setEV(Stats.SPECIAL_ATTACK, jsonObject.evs.sp_atk)
                boss.setEV(Stats.SPEED, jsonObject.evs.spd)

                boss.setIV(Stats.HP, jsonObject.ivs.hp)
                boss.setIV(Stats.DEFENCE, jsonObject.ivs.def)
                boss.setIV(Stats.SPECIAL_DEFENCE, jsonObject.ivs.sp_def)
                boss.setIV(Stats.ATTACK, jsonObject.ivs.atk)
                boss.setIV(Stats.SPECIAL_ATTACK, jsonObject.ivs.sp_atk)
                boss.setIV(Stats.SPEED, jsonObject.ivs.spd)

                val bossSettings = BossSettingsDataType.BossSettings (
                    fleeDistance = jsonObject.fleeDistance,
                    heldItem = jsonObject.heldItem,
                    needRaidPass = jsonObject.needRaidPass,
                    healthMultiplier = jsonObject.healthMultiplier?: 1.0,
                    started = false
                )

                loadedBosses[boss] = BossSettingsDataType.PokemonConfig(jsonObject.chance, bossSettings, boss)
            }
        }

        var chanceCalculator = 0.0
        for (boss in loadedBosses) {
            chanceCalculator += boss.value.chance
        }
        chanceCalculator /= 100

        val bossMap = mutableListOf<BossSettingsDataType.PokemonConfig>()
        for (boss in loadedBosses) {
            if (boss.value.chance > 0.0 || preference != "") {
                var e = (boss.value.chance / chanceCalculator * 10).toInt()
                if (e == 0) { e = 1 }
                while (e > 0) {
                    bossMap.add(boss.value)
                    e--
                }
            }
        }

        if (bossMap.isEmpty()) {
            LOGGER.error("[Compound Raids] Failed to load boss during raid creation! Check the boss config files :)")
            return null
        }

        val currentEntry = bossMap.shuffled().first()

        bossSettings = currentEntry.settings
        return currentEntry.pokemon
    }

    fun queueEmpty(): String {
        var first = ""
        var bossArr = JsonArray()

        val json = fileQueue
        json.reader().use { reader ->
            val jsonElement = JsonParser.parseReader(reader)
            if (jsonElement.isJsonArray) {
                bossArr = jsonElement.asJsonArray
                if (!bossArr.isEmpty) {
                    first = bossArr.first().asString
                }
            }
        }
        json.reader().close()

        if (first != "") {
            bossArr.remove(0)
            fileQueue.writeText(gson.toJson(bossArr))
        }

        return first
    }

    private fun getItem(namespace: String): Item {
        return CompoundRaids.server!!.worlds.first().registryManager?.get(RegistryKeys.ITEM)?.get(Identifier(namespace))!!
    }
}