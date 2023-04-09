package com.xbaimiao.easylib.module.command

import org.bukkit.command.CommandSender

abstract class CommandSpec : CommandHandler {

    companion object {
        val newCommandSpec: (String) -> CommandSpec = { command: String ->
            CommandLauncher(command, CommandSender::class.java)
        }

        inline fun <reified T : CommandSender> tNewCommandSpec(command: String): CommandSpec {
            return CommandLauncher(command, T::class.java)
        }

    }

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String = "§c你没有权限执行此命令"
    override var senderErrorMessage: String = "Incorrect sender for command"

    var root: CommandSpec? = null
    val argNodes = ArrayList<ArgNode>()

    protected var exec: (CommandContext.() -> Unit)? = null
    protected var tab: (CommandContext.() -> List<String>)? = null
    protected val subCommands = mutableMapOf<String, CommandSpec>()

    /**
     * 添加子命令
     */
    fun sub(launcher: CommandSpec) {
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

    inline fun <reified T : CommandSender> subCommand(token: String, block: CommandSpec.() -> Unit = {}) {
        sub(command<T>(token) {
            block.invoke(this)
        })
    }

    fun onlinePlayers(block: CommandSpec.() -> Unit = {}) {
        arg(onlinePlayers, block)
    }

    fun worlds(block: CommandSpec.() -> Unit = {}) {
        arg(worlds, block)
    }

    fun booleans(block: CommandSpec.() -> Unit = {}) {
        arg(booleans, block)
    }

    fun times(block: CommandSpec.() -> Unit = {}) {
        arg(times, block)
    }

    @JvmOverloads
    fun arg(argNode: ArgNode, block: CommandSpec.() -> Unit = {}) {
        argNodes.add(argNode)
        block.invoke(this)
    }

    fun arg(usage: String, block: CommandSpec.() -> Unit = {}) {
        argNodes.add(ArgNode(usage, exec = { emptyList() }))
        block.invoke(this)
    }

    fun exec(exec: CommandContext.() -> Unit) {
        this.exec = exec
    }

}