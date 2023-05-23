package com.xbaimiao.easylib.module.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.Module

/**
 * @author 小白
 * @date 2023/5/15 10:10
 **/
class ProtocolLibModule :
    PacketAdapter(EasyPlugin.getPlugin(), ListenerPriority.NORMAL, PacketType.values()), Module<EasyPlugin> {

    override fun active(plugin: EasyPlugin) {
        runCatching {
            ProtocolLibrary.getProtocolManager()
        }.onSuccess {
            it.addPacketListener(this)
        }
    }

    override fun disable(plugin: EasyPlugin) {
        runCatching {
            ProtocolLibrary.getProtocolManager()
        }.onSuccess {
            it.removePacketListener(this)
        }
    }

    override fun onPacketSending(p0: PacketEvent?) {
        p0?.let {
            if (!PacketSendEvent(p0).call()) {
                p0.isCancelled = true
            }
        }
    }

    override fun onPacketReceiving(p0: PacketEvent?) {
        p0?.let {
            if (!PacketReceiveEvent(p0).call()) {
                p0.isCancelled = true
            }
        }
    }

}