package com.xbaimiao.easylib.task

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author 小白
 * @date 2023/4/18 10:41
 **/
abstract class EasyLibBukkitTask(
    private val plugin: JavaPlugin,
) : EasyLibTask {

    override var isSync: Boolean? = false

    private val bukkitRunnable: BukkitRunnable = object : BukkitRunnable() {
        override fun run() {
            this@EasyLibBukkitTask.run()
        }
    }

    override fun runTask() {
        bukkitRunnable.runTask(plugin)
    }

    override fun runTaskLater(delay: Long) {
        bukkitRunnable.runTaskLater(plugin, delay)
    }

    override fun runTaskTimer(delay: Long, period: Long) {
        bukkitRunnable.runTaskTimer(plugin, delay, period)
    }

    override fun runTaskAsynchronously() {
        isSync = true
        bukkitRunnable.runTaskAsynchronously(plugin)
    }

    override fun runTaskLaterAsynchronously(delay: Long) {
        isSync = true
        bukkitRunnable.runTaskLaterAsynchronously(plugin, delay)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long) {
        isSync = true
        bukkitRunnable.runTaskTimerAsynchronously(plugin, delay, period)
    }

    @Synchronized
    override fun cancel() {
        bukkitRunnable.cancel()
    }

    @Synchronized
    override fun isCancelled(): Boolean {
        return bukkitRunnable.isCancelled
    }

}
