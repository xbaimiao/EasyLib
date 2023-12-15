package com.xbaimiao.easylib

import com.xbaimiao.easylib.VisitorHandler.visitor
import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.nms.MappingFile
import com.xbaimiao.easylib.nms.PacketSender
import com.xbaimiao.easylib.nms.RefRemapper
import com.xbaimiao.easylib.nms.RuntimeEnv
import com.xbaimiao.easylib.ui.UIHandler
import com.xbaimiao.easylib.util.registerListener
import com.xbaimiao.easylib.util.warn
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

    open fun disable() {}

    override fun onLoad() {
        load()
        Lang.check(this)
    }

    override fun onEnable() {
        kotlin.runCatching {
            RuntimeEnv.loadAssets(MappingFile::class.java)
            Reflex.remapper.add(RefRemapper)
        }.onFailure {
            warn("加载映射文件失败: ${it.message}")
        }
        enable()
        UIHandler.enable(this)
        registerListener(PacketSender)
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