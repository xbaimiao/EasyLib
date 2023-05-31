package com.xbaimiao.easylib.bridge.economy

import com.xbaimiao.easylib.module.utils.warn
import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

/**
 * @author 小白
 * @date 2023/5/31 09:45
 **/
class PlayerPoints : Economy {

    private val playerPointsAPI by lazy {
        (Bukkit.getPluginManager().getPlugin("PlayerPoints") as PlayerPoints?)?.api
    }

    override fun take(player: OfflinePlayer, amount: Double) {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return
        }
        playerPointsAPI!!.take(player.uniqueId, amount.toInt())
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return false
        }
        return playerPointsAPI!!.look(player.uniqueId) >= amount
    }

    override fun get(player: OfflinePlayer): Double {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return 0.0
        }
        return playerPointsAPI!!.look(player.uniqueId).toDouble()
    }

    override fun give(player: OfflinePlayer, amount: Double) {
        if (playerPointsAPI == null) {
            warn("PlayerPoints挂钩失败")
            return
        }
        playerPointsAPI!!.give(player.uniqueId, amount.toInt())
    }

}