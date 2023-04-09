package com.xbaimiao.easylib.module.command

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

interface CommandHandler : CommandExecutor, TabCompleter {

    val command: String
    var description: String?
    var permission: String?
    var permissionMessage: String
    var senderErrorMessage: String

    fun register()

    fun showHelp(sender: CommandSender)

}