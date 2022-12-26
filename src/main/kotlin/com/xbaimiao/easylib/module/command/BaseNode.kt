package com.xbaimiao.easylib.module.command

import com.xbaimiao.easylib.module.command.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.*

abstract class BaseNode<T> : ExecSpec<T> {
    var executor: (CommandExecutor.() -> Unit)? = null
    var completer: (CommandExecutor.(String) -> List<String>)? = null

    //    var argNode: ArgNode<T, *>? = null
    val subNodes: MutableList<BaseNode<*>> = mutableListOf()

    open fun node(ctx: CommandContext, beforeLast: Boolean = false): BaseNode<*> {
        permission?.let { check ->
            if (check.isNotEmpty() && !ctx.sender.hasPermission(check)) {
                permissionMessage?.let { ctx.error(it) } ?: ctx.error("permission denied")
            }
        }

        ctx.parsedNodes.add(this)

        val v = step(ctx)!!

        if (token != null) ctx.literalTokenMap[token!!] = v
        ctx.hashTokenMap[(hashToken as HashCodeToken).hashCode] = v

        if (beforeLast && ctx.args.size == 1) return this

        if (ctx.args.isEmpty()) return this

        for (subNode in subNodes) {
            if (subNode.accept(ctx)) {
                return subNode.node(ctx, beforeLast)
            }
        }
        return this
    }

    open fun accept(ctx: CommandContext): Boolean {
        if (token == null) return false
        return token!!.equals(ctx.args.firstOrNull(), true)
    }

    //    override var async: Boolean = false
    override var token: String? = null

    abstract val literalToken: String

    @Suppress("LeakingThis")
    var hashToken: ArgToken<T> = HashCodeToken(hashCode())

    override var permission: String? = null
    override var permissionMessage: Component? = null
    override var description: String = ""

    /**
     * returns parsed context value then push args array forward
     */
    abstract fun step(ctx: CommandContext): T

    override fun sub(command: CommandSpec) {
        subNodes.add(command as BaseNode<*>)
    }

    override fun command(token: String, block: CommandSpec.() -> Unit): CommandSpec {
        val sub = CommandNode()
        sub.token = token
        sub.apply(block)
        subNodes.add(sub)
        return sub
    }

    var argNodePresent: Boolean = false

    override fun arg(spec: ExecSpec<String>.(ArgToken<String>) -> Unit): ExecSpec<String> {
        if (argNodePresent) error("duplicated arg node")
        argNodePresent = true
        val argNode = ArgNode { str: String -> str }
        spec.invoke(argNode, argNode.hashToken)

        subNodes.add(argNode)
        return argNode
    }

    override fun <C> arg(parser: ArgParser<C>, spec: ExecSpec<C>.(ArgToken<C>) -> Unit): ExecSpec<C> {
        if (argNodePresent) error("duplicated arg node")
        argNodePresent = true
        val argNode = ArgNode(parser.parser)
        argNode.token = parser.argName
        if (parser.completer != null) argNode.completer = parser.completer
        spec.invoke(argNode, argNode.hashToken)
        subNodes.add(argNode)
        return argNode
    }

    override fun onlinePlayer(spec: ExecSpec<Player>.(ArgToken<Player>) -> Unit): ExecSpec<Player> =
        arg(playerParser, spec)

    override fun vararg(spec: ExecSpec<List<String>>.(ArgToken<List<String>>) -> Unit): ExecSpec<List<String>> {
        if (argNodePresent) error("duplicated arg node")
        argNodePresent = true
        val argNode = VarargNode()
        spec.invoke(argNode, argNode.hashToken)
        subNodes.add(argNode)
        return argNode
    }

    override fun completer(completer: CommandExecutor.(String) -> List<String>) {
        this.completer = completer
    }

    internal open fun feedCompletion(ex: CommandExecutor, pattern: String): List<String>? =
        completer?.invoke(ex, pattern)

    internal fun showHelp(ctx: CommandContext, root: String, stack: Stack<BaseNode<*>> = Stack()) {
        if (executor != null || (stack.isNotEmpty() && this is CommandNode)) {
            stack.push(this)
            var prefix = if (root.isEmpty()) null else Component.text(root)
            stack.forEach { node ->
                prefix = (prefix?.append(Component.text(" ")) ?: Component.empty())
                    .append(Component.text(node.literalToken).also {
                        if (node.description.isNotEmpty()) it.hoverEvent(Component.text(node.description))
                    })
            }
            val cmd = stack.last { it is CommandNode } as CommandNode
            ctx.sender.sendMessage(prefix!!.append(Component.text(" // ${cmd.description}").color(NamedTextColor.GOLD)))
            stack.pop()
        } else {
            val tokens = ArrayList<String>()
            subNodes.forEach {
                it.token?.let { token ->
                    if (tokens.contains(token)) {
                        return@forEach
                    }
                    tokens.add(token)
                }
                stack.push(this)
                it.showHelp(ctx, root, stack)
                stack.pop()
            }
        }
    }

    override fun exec(executor: CommandExecutor.() -> Unit) {
        if (this.executor != null) error("duplicated exec node")
        this.executor = executor
    }

    class HashCodeToken<T>(val hashCode: Int) : ArgToken<T>
}
