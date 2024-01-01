package com.xbaimiao.easylib

import com.xbaimiao.easylib.VisitorHandler.visitor
import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.loader.DependencyLoader
import com.xbaimiao.easylib.ui.UIHandler
import com.xbaimiao.easylib.util.MutexMap
import com.xbaimiao.easylib.util.registerListener
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    var debug = false

    init {
        init()
    }

    private fun init() {
        instance = this
    }

    open fun load() {}

    open fun enable() {}

    open fun disable() {}

    override fun onLoad() {
        Lang.check(this)
        load()
    }

    override fun onEnable() {
        UIHandler.enable(this)
        registerListener(MutexMap)
        VisitorHandler::class.java.protectionDomain.codeSource.location.visitor()
        DependencyLoader.loader(this)
        enable()
    }

    override fun onDisable() {
        UIHandler.disable()
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