package com.xbaimiao.easylib.bridge

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


fun String.replacePlaceholder(player: Player): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun String.replacePlaceholder(player: OfflinePlayer): String {
    return try {
        if (player.isOnline) {
            PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(player.uniqueId), this)
        } else if (Bukkit.getPlayer(player.uniqueId) != null) {
            PlaceholderAPI.setPlaceholders(Bukkit.getPlayer(player.uniqueId), this)
        } else {
            PlaceholderAPI.setPlaceholders(player, this)
        }
    } catch (ex: NoClassDefFoundError) {
        this
    }
}

fun List<String>.replacePlaceholder(player: Player): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}


fun List<String>.replacePlaceholder(player: OfflinePlayer): List<String> {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
    } catch (ex: NoClassDefFoundError) {
        this
    }
}
