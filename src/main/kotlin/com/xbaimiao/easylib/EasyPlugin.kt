package com.xbaimiao.easylib

import com.xbaimiao.easylib.VisitorHandler.visitor
import com.xbaimiao.easylib.chat.Lang
import com.xbaimiao.easylib.loader.DependencyLoader
import com.xbaimiao.easylib.loader.Loader
import com.xbaimiao.easylib.loader.classpath.ClassPathAppender
import com.xbaimiao.easylib.loader.classpath.ReflectionClassPathAppender
import com.xbaimiao.easylib.ui.UIHandler
import com.xbaimiao.easylib.util.LifeCycle
import com.xbaimiao.easylib.util.submit
import org.bukkit.plugin.java.JavaPlugin

abstract class EasyPlugin : JavaPlugin() {

    private val pluginEnableLazys = mutableListOf<PluginEnableLazy<*>>()
    var debug = false
    var classPathAppender: ClassPathAppender = ReflectionClassPathAppender(this.classLoader)

    init {
        init()
    }

    private fun init() {
        DependencyLoader.init(this)

        Loader.loaderKotlin(this, HashMap(), repoUrl())
        Loader.loaderLibrary(this)

        instance = this
    }

    /**
     * 自定义仓库地址
     */
    open fun repoUrl(): String {
        return Loader.ALIYUN_REPO_URL
    }

    open fun load() {}

    open fun enable() {}

    open fun active() {}

    open fun disable() {}

    override fun onLoad() {
        Lang.check(this)
        load()
    }

    override fun onEnable() {
        UIHandler.enable(this)
        VisitorHandler::class.java.protectionDomain.codeSource.location.visitor()
        DependencyLoader.loader(this)
        enable()
        pluginEnableLazys.forEach {
            it.init()
        }

        VisitorHandler.lifeCycleMethodList.forEach {
            if (it.lifeCycle == LifeCycle.ENABLE) {
                it.method.invoke(it.instance)
            }
        }

        submit {
            VisitorHandler.lifeCycleMethodList.forEach {
                if (it.lifeCycle == LifeCycle.ACTIVE) {
                    it.method.invoke(it.instance)
                }
            }
            active()
        }
    }

    override fun onDisable() {
        VisitorHandler.lifeCycleMethodList.forEach {
            if (it.lifeCycle == LifeCycle.DISABLE) {
                it.method.invoke(it.instance)
            }
        }
        UIHandler.disable()
        disable()
    }

    /**
     * 插件启用时赋值
     */
    fun <T> enable(initializer: () -> T) = PluginEnableLazy(initializer).also {
        pluginEnableLazys.add(it)
    }

    companion object {

        private lateinit var instance: EasyPlugin

        @Suppress("UNCHECKED_CAST")
        fun <T : EasyPlugin> getPlugin(): T {
            return instance as T
        }

    }

}
