package com.xbaimiao.easylib.module.utils

import com.xbaimiao.easylib.EasyPlugin

class ModuleManager<T : EasyPlugin> {

    val modules = ArrayList<Module<T>>()

    fun addModule(module: Module<T>) {
        modules.add(module)
    }

    fun removeModule(module: Module<T>) {
        modules.remove(module)
    }

    fun reloadAll() {
        disableAll()
        loadAll()
        enableAll()
    }

    fun loadAll() {
        modules.forEach { it.load(EasyPlugin.getPlugin()) }
    }

    fun enableAll() {
        modules.forEach { it.enable(EasyPlugin.getPlugin()) }
    }

    fun disableAll() {
        modules.forEach { it.disable(EasyPlugin.getPlugin()) }
    }

}