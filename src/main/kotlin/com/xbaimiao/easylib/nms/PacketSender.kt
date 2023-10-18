package com.xbaimiao.easylib.nms

import com.xbaimiao.easylib.util.submit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.ReflexClass
import java.util.concurrent.ConcurrentHashMap

object PacketSender : Listener {

    private val playerConnectionMap = ConcurrentHashMap<String, Any>()
    private var sendPacketMethod: ClassMethod? = null

    /**
     * 发送数据包
     * @param player 玩家
     * @param packet 数据包实例
     */
    fun sendPacket(player: Player, packet: Any) {
        val connection = getConnection(player)
        if (sendPacketMethod == null) {
            val reflexClass = ReflexClass.of(connection.javaClass)
            // 1.18 更名为 send 方法
            sendPacketMethod = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.Version.V1_18)) {
                try {
                    reflexClass.getMethod("a", true, true, packet)
                } catch (_: NoSuchMethodException) {
                    try {
                        reflexClass.getMethod("send", true, true, packet)
                    } catch (_: NoSuchMethodException) {
                        reflexClass.getMethod("sendPacket", true, true, packet)
                    }
                }
            } else {
                reflexClass.getMethod("sendPacket", true, true, packet)
            }
        }
        sendPacketMethod!!.invoke(connection, packet)
    }

    /**
     * 获取玩家的连接实例，如果不存在则会抛出 [NullPointerException]
     */
    fun getConnection(player: Player): Any {
        return if (playerConnectionMap.containsKey(player.name)) {
            playerConnectionMap[player.name]!!
        } else {
            val connection = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.Version.V1_20_0)) {
                player.getProperty<Any>("entity/c")!!
            } else if (MinecraftVersion.isUniversal) {
                player.getProperty<Any>("entity/connection")!!
            } else {
                player.getProperty<Any>("entity/playerConnection")!!
            }
            playerConnectionMap[player.name] = connection
            connection
        }
    }

    @EventHandler
    private fun onJoin(e: PlayerJoinEvent) {
        playerConnectionMap.remove(e.player.name)
    }

    @EventHandler
    private fun onQuit(e: PlayerQuitEvent) {
        submit(delay = 20) { playerConnectionMap.remove(e.player.name) }
    }

}