package com.xbaimiao.easylib

import org.bukkit.ChatColor

fun String?.colored(): String {
    return this?.let { ChatColor.translateAlternateColorCodes('&', it) } ?: ""
}

private val uncoloredRegex = Regex("§[a-z0-9]")
fun String?.uncolored(): String {
    return this?.replace(uncoloredRegex, "") ?: ""
}

fun List<String>.colored(): List<String> {
    return this.map { it.colored() }
}

fun List<String>.uncolored(): List<String> {
    return this.map { it.uncolored() }
}
