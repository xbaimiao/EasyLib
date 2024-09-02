package com.xbaimiao.easylib.bridge.economy

import org.bukkit.OfflinePlayer

interface Economy<T : Number> {

    /**
     * 扣除玩家货币
     */
    fun take(player: OfflinePlayer, amount: T)

    /**
     * 判断玩家是否有那么多货币
     */
    fun has(player: OfflinePlayer, amount: T): Boolean

    /**
     * 获取玩家货币数量
     */
    operator fun get(player: OfflinePlayer): T

    /**
     * 给予玩家指定数量货币
     */
    fun give(player: OfflinePlayer, amount: T)

    /**
     * 设置货币数量
     */
    fun set(player: OfflinePlayer, amount: T)

}
