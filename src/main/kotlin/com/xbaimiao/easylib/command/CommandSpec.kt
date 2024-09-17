package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.util.convertToMilliseconds
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("unused")
abstract class CommandSpec<S : CommandSender> : CommandHandler {

    companion object {

        inline fun <reified S : CommandSender> newCommandSpec(command: String): CommandSpec<S> {
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

    class CaseInsensitiveMap<V> : HashMap<String, V>() {

        override fun containsKey(key: String): Boolean {
            return super.keys.any { it.equals(key, true) }
        }

        override fun get(key: String): V? {
            return super.keys.firstOrNull { it.equals(key, true) }?.let { super.get(it) }
        }

    }

    override var description: String? = null
    override var permission: String? = null
    override var permissionMessage: String = "§c你没有权限执行此命令"
    override var senderErrorMessage: String = "§c此命令只能由 §6{sender} §c执行"

    protected var root: CommandSpec<out CommandSender>? = null
    val argNodes = ArgNodeArrayList()

    internal var exec: (CommandContext<S>.() -> Unit)? = null
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
    fun onlinePlayers(
        desc: String = "player",
        optional: Boolean = false,
        block: CommandSpec<S>.(ArgNode<Collection<Player>>) -> Unit = {},
    ): ArgNode<Collection<Player>> {
        return arg(ArgNode(desc, exec = { token ->
            arrayListOf(Bukkit.getOnlinePlayers().map { it.name }, arrayListOf("@a", "@p", "@s", "@r")).flatten()
                .filter { it.uppercase().startsWith(token.uppercase()) }
        }) { name ->
            return@ArgNode when (name.lowercase()) {
                "@a" -> Bukkit.getOnlinePlayers().toList()
                "@p" -> {
                    if (this is Player) {
                        arrayListOf(this)
                    } else {
                        arrayListOf(Bukkit.getOnlinePlayers().toList().random())
                    }
                }

                "@s" -> arrayListOf(this as Player)
                "@r" -> arrayListOf(Bukkit.getOnlinePlayers().toList().random())
                else -> Bukkit.getPlayerExact(name)?.let { arrayListOf(it) } ?: emptyList()
            }
        }, optional, block)
    }

    @JvmOverloads
    fun players(
        desc: String = "player", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Player?>) -> Unit = {},
    ): ArgNode<Player?> {
        return arg(ArgNode(desc, exec = { token ->
            Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
        }) { name ->
            Bukkit.getPlayerExact(name)
        }, optional, block)
    }

    @JvmOverloads
    fun offlinePlayers(
        desc: String = "player", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<String>) -> Unit = {},
    ): ArgNode<String> {
        return arg(ArgNode(desc, exec = { token ->
            Bukkit.getOnlinePlayers().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
        }) {
            it
        }, optional, block)
    }

    @JvmOverloads
    fun worlds(
        desc: String = "world", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<World>) -> Unit = {},
    ): ArgNode<World> {
        return arg(ArgNode(desc, exec = { token ->
            Bukkit.getWorlds().map { it.name }.filter { it.uppercase().startsWith(token.uppercase()) }
        }, parse = { name ->
            Bukkit.getWorld(name) ?: error("World $name not found")
        }), optional, block)
    }

    @JvmOverloads
    fun booleans(
        desc: String = "boolean", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Boolean>) -> Unit = {},
    ): ArgNode<Boolean> {
        return arg(ArgNode(desc, exec = { token ->
            arrayOf(desc, "true", "false").filter { it.uppercase().startsWith(token.uppercase()) }
        }, parse = {
            it.toBoolean()
        }), optional, block)
    }

    @JvmOverloads
    fun times(
        desc: String = "time", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Long>) -> Unit = {},
    ): ArgNode<Long> {
        return arg(ArgNode(desc, exec = { token ->
            arrayOf(desc, "1ms", "1s", "1m", "1h", "1d").filter { it.uppercase().startsWith(token.uppercase()) }
        }, parse = {
            convertToMilliseconds(it)
        }), optional, block)
    }

    @JvmOverloads
    fun number(
        desc: String = "number", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {},
    ): ArgNode<Double> {
        return arg(ArgNode(desc, exec = { token ->
            arrayOf(desc, "1", "2", "3", "4", "5", "number").filter { it.uppercase().startsWith(token.uppercase()) }
        }, parse = {
            it.toDouble()
        }), optional, block)
    }

    @JvmOverloads
    fun x(
        desc: String = "x", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {},
    ): ArgNode<Double> {
        return arg(ArgNode(desc, {
            if (this is Player) {
                listOf(desc, this.location.x.toString())
            } else {
                listOf(desc, "1", "2", "3", "4", "5")
            }
        }, {
            it.toDouble()
        }), optional, block)
    }

    @JvmOverloads
    fun y(
        desc: String = "y", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {},
    ): ArgNode<Double> {
        return arg(ArgNode(desc, {
            if (this is Player) {
                listOf(desc, this.location.y.toString())
            } else {
                listOf(desc, "1", "2", "3", "4", "5")
            }
        }, { it.toDouble() }), optional, block)
    }

    @JvmOverloads
    fun z(
        desc: String = "z", optional: Boolean = false, block: CommandSpec<S>.(ArgNode<Double>) -> Unit = {},
    ): ArgNode<Double> {
        return arg(ArgNode(desc, {
            if (this is Player) {
                listOf(desc, this.location.z.toString())
            } else {
                listOf(desc, "1", "2", "3", "4", "5")
            }
        }, { it.toDouble() }), optional, block)
    }

    @JvmOverloads
    fun <T> arg(
        argNode: ArgNode<T>, optional: Boolean = false, block: CommandSpec<S>.(ArgNode<T>) -> Unit,
    ): ArgNode<T> {
        return argNodes.append(argNode, optional).also { block.invoke(this, it) }
    }

    @JvmOverloads
    fun arg(
        usage: String, optional: Boolean = false, block: CommandSpec<S>.(ArgNode<String>) -> Unit,
    ): ArgNode<String> {
        val argNode = ArgNode(usage, exec = { listOf(usage) }, parse = { it })
        return argNodes.append(argNode, optional).also { block.invoke(this, it) }
    }

    @JvmOverloads
    fun <T> arg(argNode: ArgNode<T>, optional: Boolean = false): ArgNode<T> {
        return argNodes.append(argNode, optional)
    }

    fun <T> requiredArg(argNode: ArgNode<T>): ArgNode<T> {
        return argNodes.append(argNode, false)
    }

    fun <T> requiredArg(collection: Collection<T>, codec: CommandCodec<T>): ArgNode<T?> {
        val argNode = ArgNode(codec.name(), exec = { token ->
            collection.map { codec.encode(it) }.filter { it.startsWith(token) }
        }, { token ->
            collection.firstOrNull { codec.encode(it) == token }
        })
        return argNodes.append(argNode, false)
    }

    fun <T> optionalArg(argNode: ArgNode<T>): ArgNode<T> {
        return argNodes.append(argNode, true)
    }

    fun <T> optionalArg(collection: Collection<T>, codec: CommandCodec<T>): ArgNode<T?> {
        val argNode = ArgNode(codec.name(), exec = { token ->
            collection.map { codec.encode(it) }.filter { it.startsWith(token) }
        }, { token ->
            collection.firstOrNull { codec.encode(it) == token }
        })
        return argNodes.append(argNode, true)
    }

    @JvmOverloads
    fun arg(usage: String, optional: Boolean = false): ArgNode<String> {
        val argNode = ArgNode(usage, exec = { listOf(usage) }, parse = { it })
        return argNodes.append(argNode, optional)
    }

    fun exec(exec: CommandContext<S>.() -> Unit) {
        this.exec = exec
    }

}
