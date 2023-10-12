package com.xbaimiao.easylib.bridge

import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.warn
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

/**
 * @author 小白
 * @date 2023/5/18 11:51
 **/
abstract class PlaceholderExpansion {

    abstract val identifier: String

    open val version: String get() = plugin.description.version
    open val author: String get() = plugin.description.authors.joinToString(",")

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