package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * Utils
 *
 * @author xbaimiao
 * @since 2023/9/9 13:22
 */

/**
 * 将 1d 1h 1s 转为毫秒
 */
fun convertToMilliseconds(argument: String): Long {
    // 毫秒
    if (argument.endsWith("ms")) {
        return argument.substring(0, argument.length - 1).toLong()
    }
    // 秒
    if (argument.endsWith("s")) {
        return argument.substring(0, argument.length - 1).toLong() * 1000L
    }
    // 分钟
    if (argument.endsWith("m")) {
        return argument.substring(0, argument.length - 1).toLong() * 60000L
    }
    // 小时
    if (argument.endsWith("h")) {
        return argument.substring(0, argument.length - 1).toLong() * 3600000L
    }
    // 天
    if (argument.endsWith("d")) {
        return argument.substring(0, argument.length - 1).toLong() * 86400000L
    }
    return argument.toLongOrNull() ?: 0L
}

/**
 * 将毫秒转为 xx时xx分xx秒
 */
fun formatTime(time: Long): String {
    val day = time / 86400000
    val hour = time % 86400000 / 3600000
    val minute = time % 3600000 / 60000
    val second = time % 60000 / 1000
    return if (day > 0) {
        "${day}天${hour}时${minute}分${second}秒"
    } else if (hour > 0) {
        "${hour}时${minute}分${second}秒"
    } else if (minute > 0) {
        "${minute}分${second}秒"
    } else {
        "${second}秒"
    }
}

private val debugPlayers = mutableSetOf<String>()

var Player.debug: Boolean
    get() {
        return debugPlayers.contains(name)
    }
    set(value) {
        if (value && !debugPlayers.contains(name)) {
            debugPlayers.add(name)
        } else {
            debugPlayers.remove(name)
        }
    }

/**
 * 输出debug信息
 */
fun debug(vararg any: Any) {
    val message = "[DEBUG] ${any.joinToString(" ")}"
    debugPlayers.forEach {
        Bukkit.getPlayerExact(it)?.sendMessage(message)
    }
    if (EasyPlugin.getPlugin<EasyPlugin>().debug) {
        EasyPlugin.getPlugin<EasyPlugin>().logger.info(message)
    }
}

/**
 * 打印堆栈信息
 */
fun printStackTrace(plugin: JavaPlugin = EasyPlugin.getPlugin()) {
    println(
        "Print stack trace from plugin ${plugin.name} v${plugin.description.version}\r\n${
            Thread.currentThread().stackTrace.drop(
                2
            ).joinToString("\r\n") { "        at $it" }
        }"
    )
}


/**
 * 打印堆栈信息
 */
fun printStackTrace(plugin: JavaPlugin = EasyPlugin.getPlugin(), e: Throwable) {
    println(
        "Print stack trace from plugin ${plugin.name} v${plugin.description.version}\r\n$e\r\n${
            Thread.currentThread().stackTrace.drop(
                2
            ).joinToString("\r\n") { "        at $it" }
        }"
    )
}

val plugin get() = EasyPlugin.getPlugin<EasyPlugin>()