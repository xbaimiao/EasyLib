package com.xbaimiao.easylib.module.utils

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable

fun submit(
    delay: Long = 0, period: Long = 0, async: Boolean = false, task: BukkitRunnable.() -> Unit
): BukkitRunnable {

    val runnable = object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }

    if (async) {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimerAsynchronously(EasyPlugin.getPlugin(), delay, period)
            } else {
                runnable.runTaskTimerAsynchronously(EasyPlugin.getPlugin(), period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLaterAsynchronously(EasyPlugin.getPlugin(), delay)
        } else {
            runnable.runTaskAsynchronously(EasyPlugin.getPlugin())
        }
    } else {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimer(EasyPlugin.getPlugin(), delay, period)
            } else {
                runnable.runTaskTimer(EasyPlugin.getPlugin(), period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLater(EasyPlugin.getPlugin(), delay)
        } else {
            runnable.runTask(EasyPlugin.getPlugin())
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