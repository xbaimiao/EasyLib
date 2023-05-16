package com.xbaimiao.easylib.module.utils

import com.cryptomorin.xseries.XMaterial
import com.cryptomorin.xseries.messages.ActionBar
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.FoliaChecker
import com.xbaimiao.easylib.task.EasyLibBukkitTask
import com.xbaimiao.easylib.task.EasyLibFoliaTask
import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
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

fun registerListener(listener: Listener): Listener {
    EasyPlugin.getPlugin<EasyPlugin>().server.pluginManager.registerEvents(listener, EasyPlugin.getPlugin())
    return listener
}

fun unregisterListener(listener: Listener) {
    HandlerList.unregisterAll(listener)
}

fun String.parseToMaterial(): Material {
    return Material.getMaterial(this.uppercase()) ?: throw NullPointerException("$this is not a material")
}

fun String.parseToXMaterial(): XMaterial {
    return XMaterial.matchXMaterial(this).getOrNull() ?: throw NullPointerException("$this is not a XMaterial")
}

fun info(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.info(any.joinToString(" "))
}

fun warn(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.warning(any.joinToString(" "))
}

fun severe(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.severe(any.joinToString(" "))
}

fun Player.sendActionBar(message: String, plugin: EasyPlugin = EasyPlugin.getPlugin()) {
    ActionBar.sendActionBar(plugin, this, message)
}

fun Player.sendActionBarWhile(
    message: String,
    plugin: EasyPlugin = EasyPlugin.getPlugin(),
    action: () -> Boolean = { false }
) {
    ActionBar.sendActionBarWhile(plugin, this, message) {
        action()
    }
}