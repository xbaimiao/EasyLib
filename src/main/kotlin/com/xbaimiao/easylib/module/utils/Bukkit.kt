package com.xbaimiao.easylib.module.utils

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.FoliaChecker
import com.xbaimiao.easylib.task.EasyLibBukkitTask
import com.xbaimiao.easylib.task.EasyLibFoliaTask
import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.Location
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

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

fun info(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.info(any.joinToString(" "))
}

fun warn(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.warning(any.joinToString(" "))
}

fun severe(vararg any: Any) {
    EasyPlugin.getPlugin<EasyPlugin>().logger.severe(any.joinToString(" "))
}