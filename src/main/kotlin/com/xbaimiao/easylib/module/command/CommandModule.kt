package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.Module
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class CommandModule : Module<EasyPlugin> {

    init {
        driver = { token: String, spec: CommandSpec.() -> Unit ->
            val root = Launcher()
            root.token = token
            spec.invoke(root)
            root
        }
    }

    class Launcher : CommandNode(), CommandHandler {
        override fun register(plugin: JavaPlugin) {
            var cmd = plugin.getCommand(token ?: error("root command must have it's token"))
            if (cmd == null) {
                val constructor = kotlin.run {
                    PluginCommand::class.java.getDeclaredConstructor(String::class.javaObjectType, Plugin::class.java)
                }
                constructor.isAccessible = true
                cmd = constructor.newInstance(token, plugin)
                val cmdMap = plugin.server.commandMap
                cmdMap.register(plugin.name, cmd)
                plugin.server::class.java.getDeclaredMethod("syncCommands").invoke(plugin.server)
            }
            cmd!!.setExecutor(this)
            cmd.tabCompleter = this
        }

        override fun onCommand(
            sender: CommandSender,
            cmd: Command,
            label: String,
            args: Array<out String>,
        ): Boolean {
            val fullArgs = mutableListOf<String>()
            fullArgs.add(label)
            args.forEach { fullArgs.add(it) }

            val line = fullArgs.joinToString(" ")
            val ctx = CommandContext(sender, line, fullArgs)

            kotlin.runCatching {
                val node = node(ctx)
                node.executor?.invoke(ctx) ?: ctx.showHelp()
            }.getOrElse {
                if (it is CommandException) {
                    if (it.component != null) {
                        sender.sendMessage(it.component)
                    } else if (it.message.isNotEmpty()) {
                        sender.sendMessage(Component.text(it.message).color(NamedTextColor.RED))
                    }
                } else throw it
            }
            return true
        }

        override fun onTabComplete(
            sender: CommandSender,
            cmd: Command,
            label: String,
            args: Array<out String>,
        ): MutableList<String>? {
            val fullArgs = mutableListOf<String>()
            fullArgs.add(label)
            args.forEach { fullArgs.add(it) }

            val line = fullArgs.joinToString(" ")
            val ctx = CommandContext(sender, line, fullArgs)

            kotlin.runCatching {
                val node = node(ctx, true)
                val pre = if (ctx.args.isEmpty()) "" else ctx.args.first()
                val result: MutableSet<String> = mutableSetOf()
                var playerList = true
                node.subNodes.forEach {
                    it.feedCompletion(ctx, pre)?.apply {
                        playerList = false
                        result.addAll(this)
                    }
                }
                return if (playerList) null else result.toMutableList()
            }.getOrElse {
                if (it is CommandException) {
                    sender.sendMessage(it.message)
                    return mutableListOf()
                } else throw it
            }
        }
    }

}
