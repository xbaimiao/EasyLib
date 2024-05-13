package com.xbaimiao.easylib.bridge.player

import com.xbaimiao.easylib.bridge.player.EPlayerImpl.Companion.easylib
import com.xbaimiao.easylib.bridge.replacePlaceholder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author 小白
 * @date 2023/5/27 12:47
 **/
class ECommand(
    @Transient
    val list: List<String>,
    @Transient
    val player: Player,
) {

    fun exec(sender: CommandSender) {
        list.map { it.replace("%player_name%", player.name).replacePlaceholder(player) }
            .forEach {
                Bukkit.dispatchCommand(sender, it)
            }
    }

}

fun Collection<String>.autoParseExec(player: Player) {
    this.map { it.replace("%player_name%", player.name) }
        .forEach {
            if (it.startsWith("[op] ")) {
                val cmd = it.substring(5)
                cmd.parseECommand(player).exec(player.easylib().fakeOperator())
            } else if (it.startsWith("[player] ")) {
                val cmd = it.substring(9)
                cmd.parseECommand(player).exec(player)
            } else if (it.startsWith("[console] ")) {
                val cmd = it.substring(10)
                cmd.parseECommand(player).exec(Bukkit.getConsoleSender())
            }
        }
}

fun List<String>.parseECommand(player: Player): ECommand {
    return ECommand(this, player)
}

fun String.parseECommand(player: Player): ECommand {
    return ECommand(listOf(this), player)
}
