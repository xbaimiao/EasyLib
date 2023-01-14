package com.xbaimiao.easylib.module.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

fun command(command: String, block: CommandSpec.() -> Unit = {}): CommandSpec {
    val launcher = CommandSpec.newCommandSpec.invoke(command)
    block.invoke(launcher)
    return launcher
}

data class ArgNode(
    val usage: String,
    val exec: CommandSender.(String) -> List<String>
)

val onlinePlayers: ArgNode = ArgNode("player", exec = { token ->
    Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
})