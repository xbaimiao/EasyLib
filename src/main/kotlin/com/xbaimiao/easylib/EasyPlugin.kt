package com.xbaimiao.easylib

import com.xbaimiao.easylib.module.chat.Lang
import com.xbaimiao.easylib.module.ui.InventoryModule
import com.xbaimiao.easylib.module.utils.Module
import com.xbaimiao.easylib.module.utils.ModuleManager
import com.xbaimiao.easylib.module.utils.submit
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    protected lateinit var moduleManager: ModuleManager<EasyPlugin>
        private set

    var debug = false

    init {
        init()
    }

    private fun init() {
        instance = this
        moduleManager = ModuleManager()
        moduleManager.addModule(InventoryModule())
    }

    open fun load() {}

    open fun enable() {}

    open fun active() {}

    open fun disable() {}

    override fun onLoad() {
        load()
        Lang.check(this)
        moduleManager.loadAll()
    }

    override fun onEnable() {
        enable()
        moduleManager.enableAll()
        submit {
            moduleManager.modules.forEach { it.active(this@EasyPlugin) }
            active()
        }
    }

    override fun onDisable() {
        disable()
        moduleManager.disableAll()
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun ModuleManager<EasyPlugin>.addModule(module: Module<out EasyPlugin>) {
            this.addModule(module as Module<EasyPlugin>)
        }


        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}