package com.xbaimiao.easylib

import org.bukkit.scheduler.BukkitRunnable
import top.mcplugin.lib.Plugin

fun submit(
    delay: Long = 0,
    period: Long = 0,
    async: Boolean = false,
    task: BukkitRunnable.() -> Unit
) {

    val runnable = object : BukkitRunnable() {
        override fun run() {
            task()
        }
    }

    if (async) {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimerAsynchronously(Plugin.getPlugin(), delay, period)
            } else {
                runnable.runTaskTimerAsynchronously(Plugin.getPlugin(), period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLaterAsynchronously(Plugin.getPlugin(), delay)
        } else {
            runnable.runTask(Plugin.getPlugin())
        }
    } else {
        if (period > 0) {
            if (delay > 0) {
                runnable.runTaskTimer(Plugin.getPlugin(), delay, period)
            } else {
                runnable.runTaskTimer(Plugin.getPlugin(), period, period)
            }
        } else if (delay > 0) {
            runnable.runTaskLater(Plugin.getPlugin(), delay)
        } else {
            runnable.runTask(Plugin.getPlugin())
        }
    }

}