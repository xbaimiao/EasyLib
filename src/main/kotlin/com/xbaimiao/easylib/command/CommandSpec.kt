package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.util.CaseInsensitiveMap
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("unused")
abstract class CommandSpec<S : CommandSender> : CommandHandler {

    companion object {

        inline fun <reified S : CommandSender> tNewCommandSpec(command: String): CommandSpec<S> {
            return CommandLauncher(command, S::class.java)
        }

    }

    class ArgNodeArrayList : ArrayList<ArgNode<*>>() {

        override fun add(element: ArgNode<*>): Boolean {
            error("not support")
        }

        fun <T> append(argNode: ArgNode<T>, optional: Boolean): ArgNode<T> {
            val new = argNode.clone()
            new.index = this.size
            new.optional = optional
            super.add(new)
            return new
        }

    }

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String = "§c你没有权限执行此命令"
    override var senderErrorMessage: String = "§c此命令只能由 §6{sender} §c执行"

    protected var root: CommandSpec<out CommandSender>? = null
    val argNodes = ArgNodeArrayList()

    protected var exec: (CommandContext<S>.() -> Unit)? = null
    protected var tab: (CommandContext<out CommandSender>.() -> List<String>)? = null
    protected val subCommands = CaseInsensitiveMap<CommandSpec<out CommandSender>>()

    /**
     * 检查命令执行者是否有权限执行这个命令
     * @return true 有权限 false 没有权限
     */
    fun hasPermissionExec(sender: CommandSender): Boolean {
        if (permission == null) {
            return true
        }
        return sender.hasPermission(permission!!)
    }

    /**
     * 添加子命令
     */
    fun sub(launcher: CommandSpec<out CommandSender>) {
        if (tab == null) {
            tab = {
                if (args.isEmpty()) {
                    subCommands.filter { it.value.hasPermissionExec(sender) }.keys.toList()
                } else {
                    if (subCommands.containsKey(args[0])) {
                        subCommands[args[0]]!!.tab?.invoke(this) ?: emptyList()
                    } else {
                        subCommands.filter { it.value.hasPermissionExec(sender) }.keys.toList()
                            .filter { it.startsWith(args[0]) }
                    }
                }
            }
        }
        launcher.root = this
        subCommands[launcher.command] = launcher
    }

    @JvmOverloads
    inline fun <reified T : CommandSender> subCommand(token: String, block: CommandSpec<T>.() -> Unit = {}) {
        sub(command<T>(token) {
            block.invoke(this)
        })
    }

    @JvmOverloads
    fun onlinePlayers(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Collection<Player>>) -> Unit = {}) {
        arg(onlinePlayers, optional, block)
    }

    @JvmOverloads
    fun players(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Player?>) -> Unit = {}) {
        arg(onlinePlayerSingle, optional, block)
    }

    @JvmOverloads
    fun offlinePlayers(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<String>) -> Unit = {}) {
        arg(offlinePlayerSingle, optional, block)
    }

    @JvmOverloads
    fun worlds(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<World>) -> Unit = {}) {
        arg(worlds, optional, block)
    }

    @JvmOverloads
    fun booleans(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Boolean>) -> Unit = {}) {
        arg(booleans, optional, block)
    }

    @JvmOverloads
    fun times(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Long>) -> Unit = {}) {
        arg(times, optional, block)
    }

    @JvmOverloads
    fun number(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {}) {
        arg(numbers, optional, block)
    }

    @JvmOverloads
    fun x(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {}) {
        arg(x, optional, block)
    }

    @JvmOverloads
    fun y(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {}) {
        arg(y, optional, block)
    }

    @JvmOverloads
    fun z(optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {}) {
        arg(z, optional, block)
    }

    @JvmOverloads
    fun <T> arg(argNode: ArgNode<T>, optional: Boolean = false, block: CommandSpec<S>.(ArgNode<T>) -> Unit) {
        block.invoke(this, argNodes.append(argNode, optional))
    }

    @JvmOverloads
    fun arg(usage: String, optional: Boolean = false, block: CommandSpec<S>.(ArgNode<String>) -> Unit) {
        val argNode = ArgNode(usage, exec = { emptyList() }, parse = { it })
        block.invoke(this, argNodes.append(argNode, optional))
    }

    @JvmOverloads
    fun <T> arg(argNode: ArgNode<T>, optional: Boolean = false): ArgNode<T> {
        return argNodes.append(argNode, optional)
    }

    @JvmOverloads
    fun arg(usage: String, optional: Boolean = false): ArgNode<String> {
        val argNode = ArgNode(usage, exec = { emptyList() }, parse = { it })
        return argNodes.append(argNode, optional)
    }

    fun exec(exec: CommandContext<S>.() -> Unit) {
        this.exec = exec
    }

}