package com.xbaimiao.easylib.module.command

import org.bukkit.Bukkit

data class ArgNode(
    val usage: String,
    val exec: CommandContext.() -> List<String>
) {
}

fun onlinePlayers(): ArgNode {
    return ArgNode("player", exec = {
        Bukkit.getOnlinePlayers().map { it.name }
    })
}