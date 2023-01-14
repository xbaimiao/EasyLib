package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.invokeMethod
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin

class CommandLauncher(
    override val command: String
) : CommandSpec() {

    private companion object {
        const val NOT_PERMISSION_MESSAGE = "§c你没有权限执行此命令"
        const val NOT_DESCRIPTION_MESSAGE = "无描述"
    }

    override fun register() {
        val plugin = EasyPlugin.getPlugin<EasyPlugin>()
        var cmd = plugin.getCommand(command)
        if (cmd == null) {
            val constructor = kotlin.run {
                PluginCommand::class.java.getDeclaredConstructor(String::class.javaObjectType, Plugin::class.java)
            }
            constructor.isAccessible = true
            cmd = constructor.newInstance(command, plugin)

            val cmdMap = kotlin.runCatching {
                plugin.server.commandMap
            }.getOrElse { plugin.server.invokeMethod<Any>("getCommandMap") as CommandMap }

            cmdMap.register(plugin.name, cmd)

            kotlin.runCatching { plugin.server::class.java.getDeclaredMethod("syncCommands").invoke(plugin.server) }
                .getOrNull()
        }
        cmd!!.let { c ->
            permission?.let { c.permission = it }
            description?.let { c.description = it }
            c.setExecutor(this)
            c.tabCompleter = this
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): List<String>? {
        if (permission != null && !sender.hasPermission(permission!!)) {
            sender.sendMessage(permissionMessage ?: NOT_PERMISSION_MESSAGE)
            return null
        }
        val context = CommandContext(sender, cmd.name, args.toMutableList())

        // 判断参数是否足够 不足够使用 ArgNode 补全
        if (argNodes.isNotEmpty() && context.args.size <= argNodes.size) {
            val argNode = argNodes.getOrNull(args.size - 1) ?: return emptyList()
            val string = args.getOrNull(args.size - 1) ?: ""
            return argNode.exec.invoke(sender, string)
        }

        // 判断是否有子命令
        if (args.isNotEmpty()) {
            val sub = args[0]
            return if (subCommands.containsKey(sub)) {
                val newArgs = args.toMutableList()
                newArgs.removeAt(0)
                subCommands[sub]!!.onTabComplete(sender, cmd, label, newArgs.toTypedArray())
            } else {
                tab?.invoke(context)
            }
        }
        return tab?.invoke(context)
    }

    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (permission != null && !sender.hasPermission(permission!!)) {
            sender.sendMessage(permissionMessage ?: NOT_PERMISSION_MESSAGE)
            return true
        }
        val context = CommandContext(sender, cmd.name, args.toMutableList())

        // 判断参数是否足够
        if (argNodes.isNotEmpty() && args.size < argNodes.size) {
            showHelp(sender)
            return true
        }

        val common = {
            if (exec != null) {
                exec!!.invoke(context)
            } else {
                showHelp(sender)
            }
        }

        if (args.isNotEmpty()) {
            val sub = args[0]
            if (subCommands.containsKey(sub)) {
                val newArgs = args.toMutableList()
                newArgs.removeAt(0)
                return subCommands[sub]!!.onCommand(sender, cmd, label, newArgs.toTypedArray())
            } else {
                common.invoke()
            }
        } else {
            common.invoke()
        }
        return true
    }

    override fun showHelp(sender: CommandSender) {
        sender.sendMessage(" ")
        var argNodeDescription = argNodes.joinToString(" ") { "<${it.usage}>" }
        sender.sendMessage("§a$command $argNodeDescription §7- §f${description ?: NOT_DESCRIPTION_MESSAGE} ")
        subCommands.values.forEach { handler ->
            argNodeDescription = handler.argNodes.joinToString(" ") { "<${it.usage}>" }
            sender.sendMessage("§a$command ${handler.command} $argNodeDescription §7- §f${handler.description ?: NOT_DESCRIPTION_MESSAGE}")
        }
    }

}