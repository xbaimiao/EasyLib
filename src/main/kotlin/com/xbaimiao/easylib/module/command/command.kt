package com.xbaimiao.easylib.module.command

fun command(command: String, block: CommandLauncher.() -> Unit = {}): CommandHandler {
    val launcher = CommandLauncher(command)
    block.invoke(launcher)
    return launcher
}