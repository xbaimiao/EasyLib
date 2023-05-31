package com.xbaimiao.easylib.bridge.permission

import org.bukkit.entity.Player

/**
 * @author 小白
 * @date 2023/5/31 09:46
 **/
interface Permission {

    /**
     * 判断玩家是否在这个权限组
     */
    fun isPlayerInGroup(player: Player, group: String): Boolean

    /**
     * 获取玩家主权限组
     */
    fun getPrimaryGroup(player: Player): String

    /**
     * 获取玩家所在的全部权限组
     */
    fun getGroups(player: Player): Array<String>

    /**
     * 获取所有权限组
     */
    fun getGroups(): Array<String>

    /**
     * 玩家是否有对应权限
     */
    fun hasPermission(player: Player, permission: String): Boolean

    /**
     * 给予玩家权限
     */
    fun addPermission(player: Player, permission: String)

}