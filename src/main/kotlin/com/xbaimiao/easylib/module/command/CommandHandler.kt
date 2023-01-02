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

    fun register(plugin: JavaPlugin)

    fun showHelp(sender: CommandSender)

}