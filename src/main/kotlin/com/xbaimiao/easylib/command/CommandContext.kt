package com.xbaimiao.easylib.command

import com.xbaimiao.easylib.chat.colored
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

data class CommandContext<S : CommandSender>(
    val sender: S,
    val cmd: String,
    val args: MutableList<String>,
    val argNodes: List<ArgNode<*>>
) {

    init {
        if (args.isNotEmpty()) {
            if (args[args.size - 1] == "") {
                args.removeAt(args.size - 1)
            }
        }
    }

    fun <T> valueOfOrNull(argNode: ArgNode<T>): T? {
        if (argNode.optional) {
            if (args.size <= argNode.index) {
                return null
            }
        }
        return argNode.parse.invoke(sender, args[argNode.index])
    }

    fun <T> valueOf(argNode: ArgNode<T>): T {
        if (argNode.optional) {
            if (args.size <= argNode.index) {
                error("${argNode.usage} is optional but not found")
            }
        }
        return argNode.parse.invoke(sender, args[argNode.index])
    }

    fun valueToString(argNode: ArgNode<*>): String {
        return args[argNode.index]
    }

    fun <T> ArgNode<T>.valueOrNull(): T? {
        return valueOfOrNull(this)
    }

    fun <T> ArgNode<T>.value(): T {
        return valueOf(this)
    }

    fun ArgNode<*>.argString(): String {
        return valueToString(this)
    }

    fun error(any: Any?) {
        sender.sendMessage("§c${any.toString()}".colored())
    }

    fun findIntOrNull(index: Int): Int? = kotlin.runCatching { args[index].toInt() }.getOrNull()

    fun findDoubleOrNull(index: Int): Double? =
        kotlin.runCatching { args.getOrNull(index)?.toDouble() }.getOrNull()

    fun findFloatOrNull(index: Int): Float? =
        kotlin.runCatching { args.getOrNull(index)?.toFloat() }.getOrNull()

    fun findLongOrNull(index: Int): Long? = kotlin.runCatching { args.getOrNull(index)?.toLong() }.getOrNull()

    fun findShortOrNull(index: Int): Short? =
        kotlin.runCatching { args.getOrNull(index)?.toShort() }.getOrNull()

    fun findBooleanOrNull(index: Int): Boolean? = args.getOrNull(index)?.toBoolean()

    fun findPlayerOrNull(index: Int): Player? = args.getOrNull(index)?.let { Bukkit.getPlayerExact(it) }

    fun findArgOrNull(index: Int): String? = args.getOrNull(index)

    fun findArg(index: Int): String = args.getOrNull(index) ?: throw IllegalArgumentException("参数不存在")

}