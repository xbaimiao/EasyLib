package com.xbaimiao.easylib.module.command

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.min

class CommandContext(
    override val sender: CommandSender,
    override val cmd: String,
    internal var _args: MutableList<String>
) : CommandExecutor {
    override var async: Boolean = false
    override val args: List<String>
        get() = _args

    override val player: Player?
        get() = if (sender is Player) sender else null

    internal val fullArgs: List<String> = _args
    internal var fullArgsIndex = 0

    val literalTokenMap: MutableMap<String, Any> = mutableMapOf()
    val hashTokenMap: MutableMap<Int, Any> = mutableMapOf()

    override fun argError(message: String): Nothing = argError(-1, message)

    override fun argError(argIndex: Int, message: String): Nothing {
        val pref = fullArgs.subList(0, fullArgsIndex + min(args.size, argIndex) + 1).joinToString(" ")
        throw CommandException("$pref← $message")
    }

    override fun valueOf(token: String): Any? = literalTokenMap[token]

    @Suppress("UNCHECKED_CAST")
    override fun <T> valueOf(token: ArgToken<T>): T {
        val v = hashTokenMap[(token as BaseNode.HashCodeToken<T>).hashCode]
        return if (v == null) kotlin.error("value not found") else v as T
    }

    fun showHelp() {
        sender.sendMessage(Component.empty())
        closestCommandNode()?.let {
            it.showHelp(
                this, valueOf<String>(BaseNode.HashCodeToken(it.hashCode()))
            )
        }
    }

    internal var parsedNodes: MutableList<BaseNode<*>> = mutableListOf()

    private fun closestCommandNode(): CommandNode? =
        parsedNodes.lastOrNull { it is CommandNode }?.let { it as CommandNode }

}