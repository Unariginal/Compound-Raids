package me.unariginal.compound_raids.managers

import me.unariginal.compound_raids.CompoundRaids.bossSettings
import me.unariginal.compound_raids.CompoundRaids.configManager
import me.unariginal.compound_raids.CompoundRaids.server
import net.minecraft.nbt.NbtCompound
import java.util.UUID

class CreateRaid {
    fun create(): Boolean {
        bossSettings = null

        val boss = configManager.selectBoss()?: return false

        val health = (boss.hp.toDouble())

        var world = server!!.worlds.first()

        val nbtUUIDRaid = UUID.randomUUID().toString()
        val nbtList = mutableListOf<NbtCompound>()


    }
}