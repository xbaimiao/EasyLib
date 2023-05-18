package com.xbaimiao.easylib.bridge

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.warn
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * @author 小白
 * @date 2023/5/18 11:51
 **/
abstract class PlaceholderExpansion {

    abstract val identifier: String
    abstract val version: String
    open val author: String get() = EasyPlugin.getPlugin<EasyPlugin>().description.authors.joinToString(",")

    open fun onRequest(p: OfflinePlayer, params: String): String? {
        return onUUIDRequest(p.uniqueId, params)
    }

    open fun onPlaceholderRequest(p: Player, params: String): String? {
        return onUUIDRequest(p.uniqueId, params)
    }

    open fun onUUIDRequest(uuid: UUID, params: String): String? {
        return null
    }

    fun register() {
        runCatching {
            PlaceholderExpansionImpl(this).register()
        }.onFailure {
            warn("PlaceholderExpansion register failed: ${it.message}")
        }
    }

}