package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent

object SoftDependTool : Listener {

    private var master = false

    fun load() {
        Bukkit.getPluginManager().registerEvents(this, plugin)
        master = true
        hooks.forEach { it.upIfDisabled() }
    }

    fun unload() {
        HandlerList.unregisterAll(this)
        hooks.forEach { it.downIfEnabled() }
        master = false
    }

    @EventHandler
    private fun pluginUp(event: PluginEnableEvent) {
        hooks.filter { it.plugins.contains(event.plugin.name) }.forEach { it.upIfDisabled() }
    }

    @EventHandler
    private fun pluginDown(event: PluginDisableEvent) {
        hooks.filter { it.plugins.contains(event.plugin.name) }.forEach { it.downIfEnabled() }
    }

    private abstract class Hook(val plugins: List<String>) {
        var enabled = false
        fun downIfEnabled() {
            if (enabled) {
                down()
                enabled = false
            }
        }

        fun upIfDisabled() {
            if (!master) return
            if (!enabled && plugins.all { Bukkit.getPluginManager().isPluginEnabled(it) }) {
                up()
                enabled = true
            }
        }

        abstract fun down()
        abstract fun up()

        init {
            upIfDisabled()
        }
    }

    private val hooks = mutableListOf<Hook>()

    interface PluginContext {
        fun listener(createListener: () -> Listener): PluginContext
        fun task(createTask: () -> EasyLibTask): PluginContext
        fun hook(onEnable: () -> Unit, onDisable: () -> Unit): PluginContext
    }

    fun plugins(vararg plugins: String): PluginContext {
        return object : PluginContext {
            override fun listener(createListener: () -> Listener): PluginContext {
                hooks += object : Hook(plugins.toList()) {
                    var listener: Listener? = null
                    override fun down() {
                        listener?.let { HandlerList.unregisterAll(it) }
                        listener = null
                    }

                    override fun up() {
                        listener = createListener().also {
                            Bukkit.getPluginManager().registerEvents(it, plugin)
                        }
                    }
                }
                return this
            }

            override fun task(createTask: () -> EasyLibTask): PluginContext {
                hooks += object : Hook(plugins.toList()) {
                    var task: EasyLibTask? = null
                    override fun down() {
                        task?.cancel()
                        task = null
                    }

                    override fun up() {
                        task = createTask()
                    }
                }
                return this
            }

            override fun hook(onEnable: () -> Unit, onDisable: () -> Unit): PluginContext {
                hooks += object : Hook(plugins.toList()) {
                    override fun down() {
                        onDisable()
                    }

                    override fun up() {
                        onEnable()
                    }
                }
                return this
            }
        }
    }
}
