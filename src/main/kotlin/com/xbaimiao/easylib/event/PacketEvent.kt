package com.xbaimiao.easylib.event

import com.comphenix.protocol.events.PacketEvent
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

/**
 * @author 小白
 * @date 2023/5/15 10:05
 **/
abstract class PacketEvent(
    val source: PacketEvent
) : Event(), Cancellable {

    private var cancellable = false

    override fun isCancelled(): Boolean = cancellable

    override fun setCancelled(p0: Boolean) {
        cancellable = p0
    }

}