package com.xbaimiao.easylib.module.command

import org.bukkit.Bukkit

fun command(command: String, block: CommandSpec.() -> Unit = {}): CommandSpec {
    val launcher = CommandSpec.newCommandSpec.invoke(command)
    block.invoke(launcher)
    return launcher
}

data class ArgNode(
    val usage: String,
    val exec: String.(String) -> List<String>
)

val onlinePlayers: ArgNode = ArgNode("player", exec = { token ->
    Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
})