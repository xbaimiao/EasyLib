package com.xbaimiao.easylib.module.command

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class CommandLauncher(
    override val command: String
) : CommandHandler {

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String? = null

    override var exec: (CommandContext.() -> Unit)? = null
    override var tab: (CommandContext.() -> List<String>)? = null
    override var root: CommandHandler? = null

    override val argNodes = ArrayList<ArgNode>()
    private val subCommands = mutableMapOf<String, CommandHandler>()

    fun sub(launcher: CommandHandler) {
        if (tab == null) {
            tab = {
                if (args.isEmpty()) {
                    subCommands.keys.toList()
                } else {
                    if (subCommands.containsKey(args[0])) {
                        subCommands[args[0]]!!.tab?.invoke(this) ?: emptyList()
                    } else {
                        subCommands.keys.toList().filter { it.startsWith(args[0]) }
                    }
                }
            }
        }
        launcher.root = this
        subCommands[launcher.command] = launcher
    }

    fun onlinePlayers(block: CommandLauncher.() -> Unit = {}) {
        arg(onlinePlayers, block)
    }

    @JvmOverloads
    fun arg(argNode: ArgNode, block: CommandLauncher.() -> Unit = {}) {
        argNodes.add(argNode)
        block.invoke(this)
    }

    fun exec(exec: CommandContext.() -> Unit) {
        this.exec = exec
    }

    override fun register(plugin: JavaPlugin) {
        var cmd = plugin.getCommand(command)
        if (cmd == null) {
            val constructor = kotlin.run {
                PluginCommand::class.java.getDeclaredConstructor(String::class.javaObjectType, Plugin::class.java)
            }
            constructor.isAccessible = true
            cmd = constructor.newInstance(command, plugin)
            val cmdMap = plugin.server.commandMap
            cmdMap.register(plugin.name, cmd)
            plugin.server::class.java.getDeclaredMethod("syncCommands").invoke(plugin.server)
        }
        cmd!!.setExecutor(this)
        cmd.tabCompleter = this
    }

    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): List<String>? {
        if (permission != null && !sender.hasPermission(permission!!)) {
            sender.sendMessage(permissionMessage ?: "§c你没有权限执行该命令")
            return null
        }
        val context = CommandContext(sender, cmd.name, args.toMutableList())

        // 判断参数是否足够 不足够使用 ArgNode 补全
        if (argNodes.isNotEmpty() && context.args.size < argNodes.size) {
            val argNode = argNodes.getOrNull(args.size - 1) ?: return emptyList()
            val string = context.args.getOrNull(context.args.size - 1) ?: ""
            return argNode.exec.invoke(string, string)
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
            sender.sendMessage(permissionMessage ?: "§c你没有权限执行该命令")
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
        val argNodeDescription = argNodes.joinToString(" ") { "<${it.usage}>" }
        sender.sendMessage("§a$command $argNodeDescription §7- §f${description ?: "无描述"} ")
        subCommands.values.forEach { handler ->
            val argNodeDescription = handler.argNodes.joinToString(" ") { "<${it.usage}>" }
            sender.sendMessage("§a$command ${handler.command} $argNodeDescription §7- §f${handler.description ?: "无描述"}")
        }
    }

}