package com.xbaimiao.easylib.command

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

    fun showHelp(reason: ShowHelpReason, sender: CommandSender, context: CommandContext<*>)

    enum class ShowHelpReason {
        // 参数不足
        PARAMETER,

        // 普通
        NORMAL,

        // 子命令不存在
        SUB_COMMAND_NOT_FOUND
    }

}