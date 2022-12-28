package com.xbaimiao.easylib

import com.xbaimiao.easylib.module.command.CommandModule
import com.xbaimiao.easylib.module.ui.InventoryModule
import com.xbaimiao.easylib.module.utils.Module
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    init {
        init()
    }

    private lateinit var modules: ArrayList<Module<EasyPlugin>>

    private fun init() {
        instance = this
        modules = ArrayList()
        modules.add(InventoryModule())
        modules.add(CommandModule())
    }

    open fun load() {}

    open fun enable() {}

    open fun disable() {}

    override fun onLoad() {
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
    }aa

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