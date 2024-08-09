package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Supplier
import kotlin.collections.HashSet

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

/**
 * 检查这个类是否是另一个类的父类
 */
fun Class<*>.isSuperClassOf(clazz: Class<*>): Boolean {
    if (this.isAssignableFrom(clazz)) {
        return true
    }
    return this == clazz
}

fun join(args: Array<String>, start: Int = 0, separator: String = " "): String {
    return args.filterIndexed { index, _ -> index >= start }.joinToString(separator)
}

/**
 * 获取列表中特定范围内的元素
 *
 * @param list 列表
 * @param start 开始位置
 * @param end 结束位置（默认为元素数量）
 */
fun <T> subList(list: Collection<T>, start: Int = 0, end: Int = list.size): Collection<T> {
    return list.filterIndexed { index, _ -> index in start until end }
}

fun Class<*>.nonPrimitive(): Class<*> {
    return when {
        this == Integer.TYPE -> Integer::class.java
        this == Character.TYPE -> Character::class.java
        this == java.lang.Byte.TYPE -> java.lang.Byte::class.java
        this == java.lang.Long.TYPE -> java.lang.Long::class.java
        this == java.lang.Double.TYPE -> java.lang.Double::class.java
        this == java.lang.Float.TYPE -> java.lang.Float::class.java
        this == java.lang.Short.TYPE -> java.lang.Short::class.java
        this == java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        else -> this
    }
}

fun <T> lazySupplier(supplier: () -> T): Supplier<T> {
    return object : Supplier<T> {

        val value by unsafeLazy { supplier() }

        override fun get(): T {
            return value
        }
    }
}

infix fun Double.range(to: Double): ClosedRange<Double> {
    return this..to
}

infix fun ClosedRange<Double>.step(step: Double): Iterable<Double> {
    var start = this.start
    val end = this.endInclusive
    val list = HashSet<Double>()
    while (start <= end) {
        list.add(start)
        start += step
    }
    list.add(end)
    return list
}

val plugin get() = EasyPlugin.getPlugin<EasyPlugin>()

fun ByteArray.encodeBase64(): String {
    return Base64.getEncoder().encode(this).toString(StandardCharsets.UTF_8)
}

fun String.encodeBase64(): String {
    return Base64.getEncoder().encode(toByteArray()).toString(StandardCharsets.UTF_8)
}

fun ByteArray.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}

fun String.decodeBase64(): ByteArray {
    return Base64.getDecoder().decode(this)
}
