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

    val player get() = sender as? Player

    fun findIntOrNull(index: Int): Int? = args.getOrNull(index)?.toInt()

    fun findDoubleOrNull(index: Int): Double? = args.getOrNull(index)?.toDouble()

    fun findFloatOrNull(index: Int): Float? = args.getOrNull(index)?.toFloat()

    fun findLongOrNull(index: Int): Long? = args.getOrNull(index)?.toLong()

    fun findShortOrNull(index: Int): Short? = args.getOrNull(index)?.toShort()

    fun findPlayerOrNull(index: Int): Player? = args.getOrNull(index)?.let { Bukkit.getPlayerExact(it) }

    fun findArgOrNull(index: Int): String? = args.getOrNull(index)

    fun findArg(index: Int): String = args.getOrNull(index) ?: throw IllegalArgumentException("参数不存在")

}