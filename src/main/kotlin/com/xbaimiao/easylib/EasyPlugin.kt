package com.xbaimiao.easylib

import com.xbaimiao.easylib.module.command.CommandModule
import com.xbaimiao.easylib.module.ui.InventoryModule
import com.xbaimiao.easylib.module.utils.Module
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    private val modules = ArrayList<Module<EasyPlugin>>()

    open fun load() {}

    open fun enable() {}

    open fun disable() {}

    override fun onLoad() {
        instance = this
        modules.add(InventoryModule())
        modules.add(CommandModule())

        modules.forEach {
            it.load(this)
        }
        load()
    }

    override fun onEnable() {
        modules.forEach {
            it.enable(this)
        }
        enable()
    }

    override fun onDisable() {
        modules.forEach {
            it.disable(this)
        }
        disable()
    }

    companion object {

        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}