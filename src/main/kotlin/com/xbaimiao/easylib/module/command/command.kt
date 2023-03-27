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

fun registerCommand(clazz: Class<*>): Boolean {
    val header = clazz.getAnnotation(CommandHeader::class.java) ?: return false

    val subCommands = ArrayList<CommandSpec>()

    for (declaredField in clazz.declaredFields) {
        if (declaredField.getAnnotation(CommandBody::class.java) != null) {
            val commandSpec = declaredField.get(clazz) as CommandSpec
            subCommands.add(commandSpec)
        }
    }

    command(header.command) {
        if (header.description.isNotEmpty()) {
            description = header.description
        }
        if (header.permission.isNotEmpty()) {
            permission = header.permission
        }
        if (header.permissionMessage.isNotEmpty()) {
            permissionMessage = header.permissionMessage
        }
        subCommands.forEach {
            sub(it)
        }
    }.register()
    return true
}