package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.module.utils.TimeUtil
import com.xbaimiao.easylib.module.utils.warn
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Deprecated("Use command instead", replaceWith = ReplaceWith("command(command, block)"))
fun commandLegacy(command: String, block: CommandSpec.() -> Unit = {}): CommandSpec {
    val launcher = CommandSpec.newCommandSpec.invoke(command)
    block.invoke(launcher)
    return launcher
}

inline fun <reified T : CommandSender> command(
    command: String, block: CommandSpec.() -> Unit = {}
): CommandSpec {
    val launcher = CommandSpec.tNewCommandSpec<T>(command)
    block.invoke(launcher)
    return launcher
}

data class ArgNode<T>(
    val usage: String,
    val exec: CommandSender.(String) -> List<String>,
    @Suppress("UNCHECKED_CAST")
    val parse: (CommandSender.(String) -> T) = { it as T }
) {
    var index = 0

    fun clone(): ArgNode<T> {
        return ArgNode(usage, exec, parse)
    }
}

val onlinePlayers: ArgNode<Collection<Player>> = ArgNode("player", exec = { token ->
    arrayListOf(Bukkit.getOnlinePlayers().map { it.name }, arrayListOf("@a", "@p", "@s", "@r")).flatten()
        .filter { it.uppercase().startsWith(token.uppercase()) }
}) { name ->
    return@ArgNode when (name.lowercase()) {
        "@a" -> Bukkit.getOnlinePlayers().toList()
        "@p" -> {
            if (this is Player) {
                arrayListOf(this)
            } else {
                arrayListOf(Bukkit.getOnlinePlayers().toList().random())
            }
        }

        "@s" -> arrayListOf(this as Player)
        "@r" -> arrayListOf(Bukkit.getOnlinePlayers().toList().random())
        else -> arrayListOf(Bukkit.getPlayerExact(name) ?: error("Player $name not found"))
    }
}

val worlds: ArgNode<World> = ArgNode("world", exec = { token ->
    Bukkit.getWorlds().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
}, parse = { name ->
    Bukkit.getWorld(name) ?: error("World $name not found")
})

val booleans: ArgNode<Boolean> = ArgNode("boolean", exec = { token ->
    arrayOf("true", "false").filter { it.uppercase().startsWith(token.uppercase()) }
}, parse = {
    it.toBoolean()
})

val times: ArgNode<Long> = ArgNode("time", exec = { token ->
    arrayOf("1ms", "1s", "1m", "1h", "1d").filter { it.uppercase().startsWith(token.uppercase()) }
}, parse = {
    TimeUtil.analyze(it)
})

val numbers: ArgNode<Double> = ArgNode("number", exec = { token ->
    arrayOf("1", "2", "3", "4", "5", "number").filter { it.uppercase().startsWith(token.uppercase()) }
}, parse = {
    it.toDouble()
})

val x: ArgNode<Int> = ArgNode("x", {
    if (this is Player) {
        listOf(this.location.x.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, {
    it.toInt()
})

val y: ArgNode<Int> = ArgNode("y", {
    if (this is Player) {
        listOf(this.location.y.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, { it.toInt() })

val z: ArgNode<Int> = ArgNode("z", {
    if (this is Player) {
        listOf(this.location.z.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, { it.toInt() })

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