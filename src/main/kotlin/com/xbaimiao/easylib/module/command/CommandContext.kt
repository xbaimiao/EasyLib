package com.xbaimiao.easylib.module.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

data class CommandContext(
    val sender: CommandSender, val cmd: String, val args: MutableList<String>
) {

    init {
        if (args.isNotEmpty()) {
            if (args[args.size - 1] == "") {
                args.removeAt(args.size - 1)
            }
        }
    }

    fun error(any: Any) {
        sender.sendMessage("$any")
    }

    val player by lazy { sender as Player }

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