package com.xbaimiao.easylib.module.command

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

interface CommandHandler : CommandExecutor, TabCompleter {

    val command: String
    var description: String?
    var permission: String?
    var permissionMessage: String?
    var exec: (CommandContext.() -> Unit)?
    var tab: (CommandContext.() -> List<String>)?
    var root: CommandHandler?

    val argNodes: ArrayList<ArgNode>

    fun register(plugin: JavaPlugin)

    fun showHelp(sender: CommandSender)

}