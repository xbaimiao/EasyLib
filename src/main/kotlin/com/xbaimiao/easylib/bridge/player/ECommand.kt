package com.xbaimiao.easylib.bridge.player

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
    val player: Player
) {

    fun exec(sender: CommandSender) {
        list.map { it.replace("%player_name%", player.name).replacePlaceholder(player) }
            .forEach {
                Bukkit.dispatchCommand(sender, it)
            }
    }

}

fun List<String>.parseECommand(player: Player): ECommand {
    return ECommand(this, player)
}

fun String.parseECommand(player: Player): ECommand {
    return ECommand(listOf(this), player)
}