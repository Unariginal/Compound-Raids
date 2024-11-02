package me.unariginal.compound_raids.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.luckperms.api.LuckPerms
import net.luckperms.api.LuckPermsProvider
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

object RaidCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val cmdRaid = CommandManager.literal("raid").requires { src ->
            val player = src.player as? PlayerEntity
            (player != null) || src.entity == null
        }

        val cmdStart = CommandManager.literal("start").requires { src ->
            val player = src.player as? PlayerEntity
            player != null && (src.hasPermissionLevel(2) || hasLuckPerms() && getLuckPermsAPI()?.userManager?.getUser(
                player.uuid
            )!!.cachedData.permissionData.checkPermission("cc.raid.start")
                .asBoolean()) || src.entity == null
        }.then(CommandManager.argument("boss", StringArgumentType.string()).suggests(BossSuggestion())
            .executes { ctx -> startRaid(ctx) })
    }

    private fun startRaid(ctx: CommandContext<ServerCommandSource>): Int {
        var boss = StringArgumentType.getString(ctx, "boss")

        if (boss != null && boss != "random") {
            boss = boss.replace("_", " ").replace("percent", "%")
            boss = boss.split(' ').joinToString(" ") {it.replaceFirstChar(Char::uppercaseChar)}
            // add boss to Queue
        }
        //raid.start()

        return 1
    }

    private fun hasLuckPerms(): Boolean {
        return try {
            Class.forName("net.luckperms.api.LuckPerms")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun getLuckPermsAPI(): LuckPerms? {
        return try {
            LuckPermsProvider.get()
        } catch (e: IllegalStateException) {
            null
        }
    }
}