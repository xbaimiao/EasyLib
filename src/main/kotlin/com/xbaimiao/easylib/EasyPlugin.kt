package com.xbaimiao.easylib

import com.xbaimiao.easylib.VisitorHandler.visitor
import com.xbaimiao.easylib.chat.Lang
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
        load()
        Lang.check(this)
    }

    override fun onEnable() {
        enable()
        UIHandler.enable(this)
        registerListener(MutexMap)
        VisitorHandler::class.java.protectionDomain.codeSource.location.visitor()
    }

    override fun onDisable() {
        disable()
        UIHandler.disable()
    }

    companion object {

        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}