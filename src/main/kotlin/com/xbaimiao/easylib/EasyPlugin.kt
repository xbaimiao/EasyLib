package com.xbaimiao.easylib

import com.xbaimiao.easylib.module.ui.InventoryModule
import com.xbaimiao.easylib.module.utils.ModuleManager
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    init {
        init()
    }

    lateinit var moduleManager: ModuleManager<EasyPlugin>

    private fun init() {
        instance = this
        moduleManager = ModuleManager()
        moduleManager.addModule(InventoryModule())
    }

    open fun load() {}

    open fun enable() {}

    open fun disable() {}

    override fun onLoad() {
        load()
        moduleManager.loadAll()
    }

    override fun onEnable() {
        enable()
        moduleManager.enableAll()
    }

    override fun onDisable() {
        disable()
        moduleManager.disableAll()
    }

    companion object {

        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}