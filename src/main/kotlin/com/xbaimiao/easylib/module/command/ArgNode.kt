package com.xbaimiao.easylib.module.command

import org.bukkit.Bukkit

data class ArgNode(
    val usage: String,
    val exec: String.(String) -> List<String>
) {
}

val onlinePlayers: ArgNode = ArgNode("player", exec = { token ->
    Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
})