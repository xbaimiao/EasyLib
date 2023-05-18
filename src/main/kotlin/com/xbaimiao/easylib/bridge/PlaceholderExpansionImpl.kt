package com.xbaimiao.easylib.bridge

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

/**
 * @author 小白
 * @date 2023/5/18 11:50
 **/
class PlaceholderExpansionImpl(
    private val placeholderExpansion: com.xbaimiao.easylib.bridge.PlaceholderExpansion
) : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return placeholderExpansion.identifier
    }

    override fun getAuthor(): String {
        return placeholderExpansion.author
    }

    override fun getVersion(): String {
        return placeholderExpansion.version
    }

    override fun onPlaceholderRequest(p: Player?, params: String?): String? {
        if (p == null || params == null){
            return null
        }
        return placeholderExpansion.onPlaceholderRequest(p, params)
    }

    override fun onRequest(p: OfflinePlayer?, params: String?): String? {
        if (p == null || params == null){
            return null
        }
        return placeholderExpansion.onRequest(p, params)
    }

}
