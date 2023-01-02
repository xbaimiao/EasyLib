package com.xbaimiao.easylib.module.command

fun command(command: String, block: CommandSpec.() -> Unit = {}): CommandSpec {
    val launcher = CommandSpec.newCommandSpec.invoke(command)
    block.invoke(launcher)
    return launcher
}