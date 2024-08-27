package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.CommandBody
import com.xbaimiao.easylib.util.ECommandHeader
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.warn
import org.bukkit.command.CommandSender

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
    block: CommandSpec<C>.() -> Unit,
): CommandSpec<C> {
    return mainCommand(EasyPlugin.getPlugin<EasyPlugin>().description.name, block)
}

inline fun <reified C : CommandSender> mainCommand(
    debug: Boolean,
    block: CommandSpec<C>.() -> Unit,
): CommandSpec<C> {
    return mainCommand(EasyPlugin.getPlugin<EasyPlugin>().description.name, debug, block)
}

inline fun <reified C : CommandSender> mainCommand(
    command: String, block: CommandSpec<C>.() -> Unit,
): CommandSpec<C> {
    return mainCommand(command, false, block)
}

inline fun <reified C : CommandSender> mainCommand(
    command: String, debug: Boolean, block: CommandSpec<C>.() -> Unit,
): CommandSpec<C> {
    val commandSpec = command<C>(command, block)
    if (debug) {
        commandSpec.sub(debugCommand)
    }
    commandSpec.register()
    return commandSpec
}

inline fun <reified C : CommandSender> command(
    command: String, block: CommandSpec<C>.() -> Unit,
): CommandSpec<C> {
    val launcher = CommandSpec.newCommandSpec<C>(command)
    block.invoke(launcher)
    return launcher
}

data class ArgNode<T>(
    val usage: String,
    val exec: CommandSender.(String) -> List<String>,
    val parse: (CommandSender.(String) -> T),
) {

    var index = 0

    // 此参数是否可选
    var optional = false

    fun clone(): ArgNode<T> {
        return ArgNode(usage, exec, parse)
    }
}

class ArgNodeBuilder<T> {
    private var usage: String = ""
    private var exec: CommandSender.(String) -> List<String> = { emptyList() }
    private var parse: (CommandSender.(String) -> T)? = null

    fun usage(usage: String): ArgNodeBuilder<T> {
        this.usage = usage
        return this
    }

    fun compile(compile: CommandSender.(String) -> List<String>): ArgNodeBuilder<T> {
        this.exec = compile
        return this
    }

    fun parse(parse: (CommandSender.(String) -> T)): ArgNodeBuilder<T> {
        this.parse = parse
        return this
    }

    fun build(): ArgNode<T> {
        return ArgNode(usage, exec, parse ?: { it as T })
    }
}

fun <T> buildArgNode() = ArgNodeBuilder<T>()

fun registerCommand(any: Any): Boolean {
    val header = any::class.java.getAnnotation(ECommandHeader::class.java)
    if (header == null) {
        warn("The class ${any::class.java.name} is not a command class")
        return false
    }

    val subCommands = ArrayList<CommandSpec<*>>()
    if (header.debug) {
        subCommands.add(debugCommand)
    }

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
