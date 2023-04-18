package com.xbaimiao.easylib.task

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

/**
 * @author 小白
 * @date 2023/4/18 10:21
 **/
abstract class EasyLibFoliaTask(
    private val location: Location?,
    private val plugin: JavaPlugin
) : EasyLibTask {

    private lateinit var scheduledTask: ScheduledTask

    private fun runCheck() {
        if (this::scheduledTask.isInitialized) {
            error("Task is already running")
        }
    }

    override fun runTask() {
        runCheck()
        if (location == null) {
            scheduledTask = Bukkit.getGlobalRegionScheduler().run(plugin) {
                this.run()
            }
        } else {
            scheduledTask = Bukkit.getRegionScheduler().run(plugin, location) {
                this.run()
            }
        }
    }

    override fun runTaskLater(delay: Long) {
        runCheck()
        if (location == null) {
            scheduledTask = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, {
                this.run()
            }, delay)
        } else {
            scheduledTask = Bukkit.getRegionScheduler().runDelayed(plugin, location, {
                this.run()
            }, delay)
        }
    }

    override fun runTaskTimer(delay: Long, period: Long) {
        runCheck()
        if (location == null) {
            scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, {
                this.run()
            }, delay, period)
        } else {
            scheduledTask = Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, {
                this.run()
            }, delay, period)
        }
    }

    override fun runTaskAsynchronously() {
        runCheck()
        scheduledTask = Bukkit.getAsyncScheduler().runNow(plugin) {
            this.run()
        }
    }

    override fun runTaskLaterAsynchronously(delay: Long) {
        runCheck()
        scheduledTask = Bukkit.getAsyncScheduler().runDelayed(plugin, {
            this.run()
        }, delay * 50, TimeUnit.MILLISECONDS)
    }

    override fun runTaskTimerAsynchronously(delay: Long, period: Long) {
        runCheck()
        scheduledTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            this.run()
        }, delay * 50, period * 50, TimeUnit.MILLISECONDS)
    }

    @Synchronized
    override fun cancel() {
        scheduledTask.cancel()
    }

    @Synchronized
    override fun isCancelled(): Boolean {
        return scheduledTask.isCancelled
    }

}