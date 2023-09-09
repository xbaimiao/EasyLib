package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.chat.TellrawJson
import com.xbaimiao.easylib.nms.MinecraftVersion
import com.xbaimiao.easylib.util.Strings
import com.xbaimiao.easylib.util.invokeMethod
import com.xbaimiao.easylib.util.isSuperClassOf
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin

class CommandLauncher<T : CommandSender>(
    override val command: String,
    private val execClass: Class<out CommandSender>
) : CommandSpec<T>() {

    private companion object {
        const val NOT_DESCRIPTION_MESSAGE = "没有描述"
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

    @Suppress("UNCHECKED_CAST")
    override fun onTabComplete(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): List<String>? {
        if (!execClass.isSuperClassOf(sender::class.java)) {
            return null
        }
        if (!hasPermissionExec(sender)) {
            return null
        }
        val context = CommandContext(sender as T, cmd.name, args.toMutableList(), argNodes)

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

    @Suppress("UNCHECKED_CAST")
    override fun onCommand(
        sender: CommandSender,
        cmd: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!execClass.isSuperClassOf(sender::class.java)) {
            sender.sendMessage(senderErrorMessage.replace("{sender}", execClass.simpleName))
            return true
        }
        if (!hasPermissionExec(sender)) {
            sender.sendMessage(permissionMessage)
            return true
        }
        val context = CommandContext(sender as T, cmd.name, args.toMutableList(), argNodes)

        // 判断参数是否足够
        if (argNodes.isNotEmpty() && args.size < argNodes.size) {
            for ((index, argNode) in argNodes.withIndex()) {
                if (!argNode.optional && args.getOrNull(index) == null) {
                    showHelp(CommandHandler.ShowHelpReason.PARAMETER, sender, context)
                    return true
                }
            }
        }

        val common = { reason: CommandHandler.ShowHelpReason ->
            if (exec != null) {
                exec!!.invoke(context)
            } else {
                showHelp(reason, sender, context)
            }
        }

        if (args.isNotEmpty()) {
            val sub = args[0]
            if (subCommands.containsKey(sub)) {
                val newArgs = args.toMutableList()
                newArgs.removeAt(0)
                return subCommands[sub]!!.onCommand(sender, cmd, label, newArgs.toTypedArray())
            } else {
                common.invoke(CommandHandler.ShowHelpReason.SUB_COMMAND_NOT_FOUND)
            }
        } else {
            common.invoke(CommandHandler.ShowHelpReason.NORMAL)
        }
        return true
    }

    override fun showHelp(reason: CommandHandler.ShowHelpReason, sender: CommandSender, context: CommandContext<*>) {
        val roots = this.getRootCommands()
        val displayCommand = (roots.joinToString(" ") { it.command } + " $command").trim()

        val showAll: Boolean

        when (reason) {
            CommandHandler.ShowHelpReason.SUB_COMMAND_NOT_FOUND -> {
                if (context.args.size > 1) {
                    sender.sendMessage(" ")
                    sender.sendMessage("§7指令 §f$displayCommand §7参数有误.")
                    sender.sendMessage("§7正确用法:")
                    showAll = true
                } else {
                    val arg0 = context.args[0]
                    val similar = subCommands
                        .values
                        .asSequence()
                        .filter { it.hasPermissionExec(sender) }
                        .maxByOrNull { Strings.similarDegree(it.command, context.args[0]) }
                    sender.sendMessage("§7指令 §f$displayCommand $arg0 §7不存在.")
                    sender.sendMessage("§7你可能想要:")
                    sender.sendMessage("§7/$displayCommand ${similar?.command}")
                    showAll = false
                }
            }

            CommandHandler.ShowHelpReason.PARAMETER -> {
                val usage = argNodes.joinToString(" ") { it.toDesc() }
                sender.sendMessage(" ")
                sender.sendMessage("§7指令 §f$displayCommand §7参数不足.")
                sender.sendMessage("§7正确用法:")
                sender.sendMessage("§f/$displayCommand $usage §8- §7${description ?: NOT_DESCRIPTION_MESSAGE}")
                showAll = false
            }

            CommandHandler.ShowHelpReason.NORMAL -> {
                showAll = true
            }
        }

        if (showAll) {
            sender.sendMessage(" ")
            val plugin = EasyPlugin.getPlugin<EasyPlugin>()
            val pluginVersion = plugin.description.version
            TellrawJson()
                .append("  ").append(" §7${plugin.name}")
                .hoverText("§7${plugin.name} ${if (roots.isEmpty()) description else roots[0].description}")
                .append(" ").append(" §f${pluginVersion}")
                .hoverText(
                    """
                §7插件版本: §2${pluginVersion}
                §7游戏版本: §b${MinecraftVersion.currentVersion}
                    """.trimIndent()
                ).sendTo(sender)
            sender.sendMessage(" ")
            TellrawJson()
                .append("  §7命令: ").append("§f/$displayCommand §8[...]")
                .hoverText("§f/$displayCommand §8[...]")
                .suggestCommand("/$displayCommand ")
                .sendTo(sender)
            sender.sendMessage("  §7参数: ${argNodes.joinToString(" ") { it.toDesc() }}")

            subCommands.values
                .filter { it.hasPermissionExec(sender) }
                .forEach { sub ->
                    val name = sub.command
                    val usage = sub.argNodes.joinToString(" ") { it.toDesc() }
                    val description = sub.description ?: NOT_DESCRIPTION_MESSAGE
                    TellrawJson()
                        .append("    §8- §f$name $usage")
                        .hoverText("§f/$command $name $usage §8- §7$description")
                        .suggestCommand("/$command $name ")
                        .sendTo(sender)

                    sender.sendMessage("      §7$description")
                }
        }

    }

    private fun ArgNode<*>.toDesc(): String {
        return if (optional) {
            "§a<${usage}>"
        } else {
            "§c<${usage}>"
        }
    }

    private fun getRootCommands(): List<CommandSpec<*>> {
        val list = mutableListOf<CommandSpec<*>>()
        this.root?.let {
            if (it is CommandLauncher) {
                list.addAll(it.getRootCommands())
            }
            list.add(it)
        }
        return list
    }

}