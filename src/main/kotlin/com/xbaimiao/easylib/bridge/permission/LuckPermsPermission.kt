package com.xbaimiao.easylib.bridge.permission

import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import org.bukkit.entity.Player


/**
 * @author 小白
 * @date 2023/5/31 10:16
 **/
class LuckPermsPermission : Permission {

    private val api by lazy {
        LuckPermsProvider.get()
    }

    override fun isPlayerInGroup(player: Player, group: String): Boolean {
        return player.hasPermission("group.$group")
    }

    override fun getPrimaryGroup(player: Player): String {
        return getUser(player).primaryGroup
    }

    override fun getGroups(player: Player): Array<String> {
        val user = getUser(player)
        return user.getInheritedGroups(user.queryOptions).map { it.name }.toTypedArray()
    }

    override fun getGroups(): Array<String> {
        return api.groupManager.loadedGroups.map { it.name }.toTypedArray()
    }

    private fun getUser(player: Player): User {
        var user = api.userManager.getUser(player.uniqueId)
        if (user == null) {
            user = api.userManager.loadUser(player.uniqueId).get()
        }
        return user!!
    }

}