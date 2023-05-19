package com.xbaimiao.easylib.module.utils

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.messages.ActionBar
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.FoliaChecker
import com.xbaimiao.easylib.task.EasyLibBukkitTask
import com.xbaimiao.easylib.task.EasyLibFoliaTask
import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.optionals.getOrNull

/**
 * 使用调度器执行一段代码
 * @param delay 延迟执行的时间
 * @param period 重复执行的时间
 * @param async 是否异步执行
 * @param location 位置(仅在folia核心中生效)
 * @param task 任务
 */
fun submit(
    delay: Long = 0,
    period: Long = 0,
    async: Boolean = false,
    location: Location? = null,
    task: EasyLibTask.() -> Unit
): EasyLibTask {

    val runnable by lazy {
        if (FoliaChecker.isFolia()) {
            object : EasyLibFoliaTask(location, EasyPlugin.getPlugin()) {
                override fun run() {
                    task()
                }
            }
        } else {
            object : EasyLibBukkitTask(EasyPlugin.getPlugin()) {
                override fun run() {
                    task()
                }
            }
        }
    }

    if (async) {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimerAsynchronously(delay, period)
            } else {
                runnable.runTaskTimerAsynchronously(period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLaterAsynchronously(delay)
        } else {
            runnable.runTaskAsynchronously()
        }
    } else {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimer(delay, period)
            } else {
                runnable.runTaskTimer(period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLater(delay)
        } else {
            runnable.runTask()
        }
    }

    return runnable
}

/**
 * 注册一个监听器
 */
fun registerListener(listener: Listener): Listener {
    EasyPlugin.getPlugin<EasyPlugin>().server.pluginManager.registerEvents(listener, EasyPlugin.getPlugin())
    return listener
}

/**
 * 取消注册一个监听器
 */
fun unregisterListener(listener: Listener) {
    HandlerList.unregisterAll(listener)
}

/**
 * 将字符串转换为Material
 */
fun String.parseToMaterial(): Material {
    return Material.getMaterial(this.uppercase()) ?: throw NullPointerException("$this is not a material")
}

/**
 * 将字符串转换为XMaterial
 */
fun String.parseToXMaterial(): XMaterial {
    return XMaterial.matchXMaterial(this).getOrNull() ?: throw NullPointerException("$this is not a XMaterial")
}

/**
 * 打印一段信息
 */
fun info(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.info(any.joinToString(" "))
}

/**
 * 输出警告信息
 */
fun warn(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.warning(any.joinToString(" "))
}

/**
 * 输出严重信息
 */
fun severe(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.severe(any.joinToString(" "))
}

/**
 * 发送ActionBar
 */
fun Player.sendActionBar(message: String, plugin: EasyPlugin = EasyPlugin.getPlugin()) {
    ActionBar.sendActionBar(plugin, this, message)
}

/**
 * 发送ActionBar
 */
fun Player.sendActionBarWhile(
    message: String,
    plugin: EasyPlugin = EasyPlugin.getPlugin(),
    action: () -> Boolean = { false }
) {
    ActionBar.sendActionBarWhile(plugin, this, message) {
        action()
    }
}

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