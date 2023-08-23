package com.xbaimiao.easylib.module.utils

import com.cryptomorin.xseries.XMaterial
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.ServerChecker
import com.xbaimiao.easylib.module.chat.colored
import com.xbaimiao.easylib.module.chat.uncolored
import com.xbaimiao.easylib.task.EasyLibBukkitTask
import com.xbaimiao.easylib.task.EasyLibFoliaTask
import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

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
    maxRunningNum: Long = 0,
    task: EasyLibTask.() -> Unit
): EasyLibTask {

    var currentRunningNum = 0

    val runningTask = runningTask@{ easyLibTask: EasyLibTask ->
        // 如果设置了最大运行数量, 则在达到最大运行数量时取消任务
        if (maxRunningNum > 0 && currentRunningNum++ >= maxRunningNum) {
            easyLibTask.cancel()
            return@runningTask
        }
        task(easyLibTask)
    }


    val runnable by lazy {
        if (ServerChecker.isFolia) {
            object : EasyLibFoliaTask(location, EasyPlugin.getPlugin()) {
                override fun run() {
                    runningTask(this)
                }
            }
        } else {
            object : EasyLibBukkitTask(EasyPlugin.getPlugin()) {
                override fun run() {
                    runningTask(this)
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

fun <T> Optional<T>.getOrNull(): T? {
    return if (this.isPresent) {
        this.get()
    } else {
        null
    }
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

@Deprecated(
    "use com.xbaimiao.easylib.module.chat.UtilKt",
    ReplaceWith("this?.colored() ?: \"\"", "com.xbaimiao.easylib.module.chat.colored")
)
fun String?.colored(): String {
    return this?.colored() ?: ""
}

@Deprecated(
    "use com.xbaimiao.easylib.module.chat.UtilKt",
    ReplaceWith("this?.uncolored() ?: \"\"", "com.xbaimiao.easylib.module.chat.uncolored")
)
fun String?.uncolored(): String {
    return this?.uncolored() ?: ""
}

@Deprecated(
    "use com.xbaimiao.easylib.module.chat.UtilKt",
    ReplaceWith("this.colored()", "com.xbaimiao.easylib.module.chat.colored")
)
fun List<String>.colored(): List<String> {
    return this.colored()
}

@Deprecated(
    "use com.xbaimiao.easylib.module.chat.UtilKt",
    ReplaceWith("this.uncolored()", "com.xbaimiao.easylib.module.chat.uncolored")
)
fun List<String>.uncolored(): List<String> {
    return this.uncolored()
}

fun onlinePlayers(): List<Player> {
    return Bukkit.getOnlinePlayers().toList()
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