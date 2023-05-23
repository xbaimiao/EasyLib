package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.module.utils.warn
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@Deprecated("Use command instead", replaceWith = ReplaceWith("command(command, block)"))
fun commandLegacy(command: String, block: CommandSpec.() -> Unit = {}): CommandSpec {
    val launcher = CommandSpec.newCommandSpec.invoke(command)
    block.invoke(launcher)
    return launcher
}

inline fun <reified T : CommandSender> command(
    command: String,
    block: CommandSpec.() -> Unit = {}
): CommandSpec {
    val launcher = CommandSpec.tNewCommandSpec<T>(command)
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

val worlds: ArgNode = ArgNode("world", exec = { token ->
    Bukkit.getWorlds().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
})

val booleans: ArgNode = ArgNode("boolean", exec = { token ->
    arrayOf("true", "false").filter { it.uppercase().startsWith(token.uppercase()) }
})

val times: ArgNode = ArgNode("time", exec = { token ->
    arrayOf("1ms", "1s", "1m", "1h", "1d").filter { it.uppercase().startsWith(token.uppercase()) }
})

val numbers: ArgNode = ArgNode("number", exec = { token ->
    arrayOf("1", "2", "3", "4", "5", "number").filter { it.uppercase().startsWith(token.uppercase()) }
})

@Suppress("unused")
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

    command<CommandSender>(header.command) {
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