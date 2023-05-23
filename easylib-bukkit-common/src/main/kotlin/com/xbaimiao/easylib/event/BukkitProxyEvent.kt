package com.xbaimiao.easylib.event

import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * @author 小白
 * @date 2023/5/23 13:45
 **/
@Suppress("SameReturnValue")
open class BukkitProxyEvent(async: Boolean = !Bukkit.isPrimaryThread()) : Event(async), Cancellable {

    private var isCancelled = false

    open val allowCancelled: Boolean
        get() = true

    override fun getHandlers(): HandlerList {
        return getHandlerList()
    }

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(value: Boolean) {
        if (allowCancelled) {
            isCancelled = value
        } else {
            error("unsupported")
        }
    }

    /**
     * 广播这个事件
     * @return 如果事件未被取消则为true
     */
    fun call(): Boolean {
        Bukkit.getPluginManager().callEvent(this)
        return !isCancelled
    }

    companion object {

        @JvmField
        val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }

    }

}