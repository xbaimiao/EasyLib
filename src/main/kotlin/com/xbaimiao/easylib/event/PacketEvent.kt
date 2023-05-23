package com.xbaimiao.easylib.event


/**
 * @author 小白
 * @date 2023/5/15 10:05
 **/
abstract class PacketEvent(
    val source: com.comphenix.protocol.events.PacketEvent
) : BukkitProxyEvent(source.isAsync)

class PacketReceiveEvent(source: com.comphenix.protocol.events.PacketEvent) : PacketEvent(source)

class PacketSendEvent(source: com.comphenix.protocol.events.PacketEvent) : PacketEvent(source)