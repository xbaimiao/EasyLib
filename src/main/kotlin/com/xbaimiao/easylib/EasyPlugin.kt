package com.xbaimiao.easylib

import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.nms.PacketSender
import com.xbaimiao.easylib.nms.RefRemapper
import com.xbaimiao.easylib.ui.UIHandler
import com.xbaimiao.easylib.util.registerListener
import com.xbaimiao.easylib.util.submit
import org.bukkit.plugin.java.JavaPlugin
import org.tabooproject.reflex.Reflex

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

    open fun active() {}

    open fun disable() {}

    override fun onLoad() {
        Reflex.remapper.add(RefRemapper)
        load()
        Lang.check(this)
    }

    override fun onEnable() {
        enable()
        UIHandler.enable(this)
        registerListener(PacketSender)
        submit {
            active()
        }
    }

    override fun onDisable() {
        disable()
        UIHandler.disable(this)
    }

    companion object {

        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}