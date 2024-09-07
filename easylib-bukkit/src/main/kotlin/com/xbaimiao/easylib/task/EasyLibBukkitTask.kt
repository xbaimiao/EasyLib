package com.xbaimiao.easylib.task

import com.github.Anon8281.universalScheduler.UniversalRunnable
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author 小白, BaiShenYoa_Dog
 * @date 2023/9/7 23:22
 **/
abstract class EasyLibBukkitTask(
    private val plugin: JavaPlugin
) : EasyLibTask {

    override var isSync: Boolean? = false

    private val runnable: UniversalRunnable = object : UniversalRunnable() {
        override fun run() {
            this@EasyLibBukkitTask.run()
        }
    }

    override fun runTask() {
        runnable.runTask(plugin)
    }

    override fun runTaskLater(delay: Long) {
        runnable.runTaskLater(plugin, delay)
    }

    override fun runTaskTimer(delay: Long, period: Long) {
        runnable.runTaskTimer(plugin, delay, period)
    }

    override fun runTaskAsynchronously() {
        isSync = true
        runnable.runTaskAsynchronously(plugin)
    }

    override fun runTaskLaterAsynchronously(delay: Long) {
        isSync = true
        runnable.runTaskLaterAsynchronously(plugin, delay)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long) {
        isSync = true
        runnable.runTaskTimerAsynchronously(plugin, delay, period)
    }

    @Synchronized
    override fun cancel() {
        runnable.cancel()
    }

    @Synchronized
    override fun isCancelled(): Boolean {
        return runnable.isCancelled
    }

}