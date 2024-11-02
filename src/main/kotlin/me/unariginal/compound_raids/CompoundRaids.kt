package me.unariginal.compound_raids

import me.unariginal.compound_raids.commands.RaidCommand
import me.unariginal.compound_raids.config.ConfigManager
import me.unariginal.compound_raids.datatypes.BossSettingsDataType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import org.apache.logging.log4j.LogManager

object CompoundRaids : ModInitializer {
    const val MODID = "compound-raids"
    val LOGGER = LogManager.getLogger()

    var server: MinecraftServer? = null
    var world: ServerWorld? = null

    val configManager = ConfigManager()

    var bossSettings: BossSettingsDataType.BossSettings? = null

    override fun onInitialize() {
        LOGGER.info("[Compound Raids] It's battle time!")

        ServerTickEvents.START_SERVER_TICK.register { server ->
            this.server = server
            world = server.worlds.first()

            RaidCommand.register(server.commandManager.dispatcher)
        }

    }
}
