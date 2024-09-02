package com.xbaimiao.easylib.bridge.economy

import com.xbaimiao.easylib.util.warn
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * @author 小白
 * @date 2023/5/31 09:45
 **/
class PlayerPoints : Economy<Int> {

    private val playerPointsAPI by lazy {
        (Bukkit.getPluginManager().getPlugin("PlayerPoints") as PlayerPoints?)?.api
    }

    override fun take(player: OfflinePlayer, amount: Int) {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return
        }
        playerPointsAPI!!.take(player.uniqueId, amount)
    }

    override fun has(player: OfflinePlayer, amount: Int): Boolean {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return false
        }
        return playerPointsAPI!!.look(player.uniqueId) >= amount
    }

    override fun get(player: OfflinePlayer): Int {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return 0
        }
        return playerPointsAPI!!.look(player.uniqueId)
    }

    override fun give(player: OfflinePlayer, amount: Int) {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return
        }
        playerPointsAPI!!.give(player.uniqueId, amount)
    }

    override fun set(player: OfflinePlayer, amount: Int) {
        playerPointsAPI!!.set(player.uniqueId, amount)
    }

}
