package com.xbaimiao.easylib.event

import org.bukkit.event.HandlerList

/**
 * @author 小白
 * @date 2023/5/15 10:07
 **/
class PacketReceiveEvent(source: com.comphenix.protocol.events.PacketEvent) : PacketEvent(source) {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        @JvmField
        val handlerList = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlerList
        }
    }

}