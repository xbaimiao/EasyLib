package com.xbaimiao.easylib.module.utils

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player


fun String.replacePlaceholder(player: Player): String {
    return try {
        PlaceholderAPI.setPlaceholders(player, this)
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
