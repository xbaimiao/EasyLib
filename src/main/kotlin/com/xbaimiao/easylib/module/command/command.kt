package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.module.utils.warn
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
    val header = clazz.getAnnotation(CommandHeader::class.java)
    if (header == null) {
        warn("The class ${clazz.name} is not a command class")
        return false
    }

    val subCommands = ArrayList<CommandSpec>()

    val instance = runCatching {
        val instance = clazz.getDeclaredField("INSTANCE")
        instance.isAccessible = true
        instance.get(clazz)
    }.getOrElse { clazz.newInstance() }

    for (declaredField in clazz.declaredFields) {
        if (declaredField.getAnnotation(CommandBody::class.java) != null) {
            declaredField.isAccessible = true
            val commandSpec = declaredField.get(instance) as CommandSpec
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