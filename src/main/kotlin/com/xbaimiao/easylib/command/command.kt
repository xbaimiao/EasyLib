package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.*
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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

val onlinePlayerSingle = ArgNode("player", exec = { token ->
    Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
}) { name ->
    Bukkit.getPlayerExact(name)
}

val offlinePlayerSingle = ArgNode("player", exec = { token ->
    Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
}) {
    it
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
    convertToMilliseconds(it)
})

val numbers: ArgNode<Double> = ArgNode("number", exec = { token ->
    arrayOf("1", "2", "3", "4", "5", "number").filter { it.uppercase().startsWith(token.uppercase()) }
}, parse = {
    it.toDouble()
})

val x: ArgNode<Double> = ArgNode("x", {
    if (this is Player) {
        listOf(this.location.x.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, {
    it.toDouble()
})

val y: ArgNode<Double> = ArgNode("y", {
    if (this is Player) {
        listOf(this.location.y.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, { it.toDouble() })

val z: ArgNode<Double> = ArgNode("z", {
    if (this is Player) {
        listOf(this.location.z.toString())
    } else {
        listOf("1", "2", "3", "4", "5")
    }
}, { it.toDouble() })


val debugCommand = command<CommandSender>("debug") {
    permission = "op"
    description = "debug"
    onlinePlayers(optional = true) { playerArg ->
        exec {
            val players = valueOfOrNull(playerArg)
            if (players == null) {
                EasyPlugin.getPlugin<EasyPlugin>().debug = !EasyPlugin.getPlugin<EasyPlugin>().debug
                sender.sendMessage("§a已${if (EasyPlugin.getPlugin<EasyPlugin>().debug) "开启" else "关闭"}debug模式")
            } else {
                players.forEach {
                    it.debug = !it.debug
                }
            }
        }
    }
}

inline fun <reified C : CommandSender> mainCommand(
    block: CommandSpec<C>.() -> Unit
): CommandSpec<C> {
    return mainCommand(EasyPlugin.getPlugin<EasyPlugin>().description.name, block)
}

inline fun <reified C : CommandSender> mainCommand(
    debug: Boolean,
    block: CommandSpec<C>.() -> Unit
): CommandSpec<C> {
    return mainCommand(EasyPlugin.getPlugin<EasyPlugin>().description.name, debug, block)
}

inline fun <reified C : CommandSender> mainCommand(
    command: String, block: CommandSpec<C>.() -> Unit
): CommandSpec<C> {
    return mainCommand(command, false, block)
}

inline fun <reified C : CommandSender> mainCommand(
    command: String, debug: Boolean, block: CommandSpec<C>.() -> Unit
): CommandSpec<C> {
    val commandSpec = command<C>(command, block)
    if (debug) {
        commandSpec.sub(debugCommand)
    }
    commandSpec.register()
    return commandSpec
}

inline fun <reified C : CommandSender> command(
    command: String, block: CommandSpec<C>.() -> Unit
): CommandSpec<C> {
    val launcher = CommandSpec.tNewCommandSpec<C>(command)
    block.invoke(launcher)
    return launcher
}

data class ArgNode<T>(
    val usage: String,
    val exec: CommandSender.(String) -> List<String>,
    val parse: (CommandSender.(String) -> T)
) {

    var index = 0

    // 此参数是否可选
    var optional = false

    fun clone(): ArgNode<T> {
        return ArgNode(usage, exec, parse)
    }
}

fun registerCommand(any: Any): Boolean {
    val header = any::class.java.getAnnotation(CommandHeader::class.java)
    if (header == null) {
        warn("The class ${any::class.java.name} is not a command class")
        return false
    }

    val subCommands = ArrayList<CommandSpec<*>>()

    for (declaredField in any::class.java.declaredFields) {
        if (declaredField.getAnnotation(CommandBody::class.java) != null) {
            declaredField.isAccessible = true
            val commandSpec = declaredField.get(any) as CommandSpec<*>
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
