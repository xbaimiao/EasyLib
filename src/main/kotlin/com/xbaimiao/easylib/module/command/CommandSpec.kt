package com.xbaimiao.easylib.module.command

import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class CommandSpec : CommandHandler {

    companion object {
        val newCommandSpec: (String) -> CommandSpec = { command: String ->
            CommandLauncher(command, CommandSender::class.java)
        }

        inline fun <reified T : CommandSender> tNewCommandSpec(command: String): CommandSpec {
            return CommandLauncher(command, T::class.java)
        }

    }

    class ArgNodeArrayList : ArrayList<ArgNode<*>>() {

        override fun add(element: ArgNode<*>): Boolean {
            error("not support")
        }

        fun <T> append(argNode: ArgNode<T>): ArgNode<T> {
            val new = argNode.clone()
            new.index = this.size
            super.add(new)
            return new
        }

    }

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String = "§c你没有权限执行此命令"
    override var senderErrorMessage: String = "Incorrect sender for command"

    var root: CommandSpec? = null
    val argNodes = ArgNodeArrayList()

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

    fun onlinePlayers(block: CommandSpec.(ArgNode<Collection<Player>>) -> Unit = {}) {
        arg(onlinePlayers, block)
    }

    fun worlds(block: CommandSpec.(ArgNode<World>) -> Unit = {}) {
        arg(worlds, block)
    }

    fun booleans(block: CommandSpec.(ArgNode<Boolean>) -> Unit = {}) {
        arg(booleans, block)
    }

    fun times(block: CommandSpec.(ArgNode<Long>) -> Unit = {}) {
        arg(times, block)
    }

    fun number(block: CommandSpec.(ArgNode<Double>) -> Unit = {}) {
        arg(numbers, block)
    }

    fun x(block: CommandSpec.(ArgNode<Int>) -> Unit = {}) {
        arg(x, block)
    }

    fun y(block: CommandSpec.(ArgNode<Int>) -> Unit = {}) {
        arg(y, block)
    }

    fun z(block: CommandSpec.(ArgNode<Int>) -> Unit = {}) {
        arg(z, block)
    }

    @JvmOverloads
    fun <T> arg(argNode: ArgNode<T>, block: CommandSpec.(ArgNode<T>) -> Unit = {}) {
        block.invoke(this, argNodes.append(argNode))
    }

    fun arg(usage: String, block: CommandSpec.(ArgNode<String>) -> Unit = {}) {
        val argNode = ArgNode<String>(usage, exec = { emptyList() })
        block.invoke(this, argNodes.append(argNode))
    }

    fun <T> arg(argNode: ArgNode<T>): ArgNode<T> {
        return argNodes.append(argNode)
    }

    fun arg(usage: String): ArgNode<String> {
        val argNode = ArgNode<String>(usage, exec = { emptyList() })
        return argNodes.append(argNode)
    }

    fun exec(exec: CommandContext.() -> Unit) {
        this.exec = exec
    }

}