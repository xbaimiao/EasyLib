package com.xbaimiao.easylib.bridge.economy

import org.bukkit.OfflinePlayer

interface Economy {

    /**
     * 扣除玩家货币
     */
    fun take(player: OfflinePlayer, amount: Double)

    /**
     * 判断玩家是否有那么多货币
     */
    fun has(player: OfflinePlayer, amount: Double): Boolean

    /**
     * 获取玩家货币数量
     */
    operator fun get(player: OfflinePlayer): Double

    /**
     * 给予玩家指定数量货币
     */
    fun give(player: OfflinePlayer, amount: Double)

    /**
     * 设置货币数量
     */
    fun set(player: OfflinePlayer, amount: Double)

}
