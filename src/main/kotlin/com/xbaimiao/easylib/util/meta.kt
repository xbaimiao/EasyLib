package com.xbaimiao.easylib.util

import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.Metadatable

/**
 * @author xbaimiao
 * @date 2024/5/25
 * @email owner@xbaimiao.com
 */
fun Metadatable.setMeta(key: String, value: Any) {
    setMetadata(key, FixedMetadataValue(plugin, value))
}

fun Metadatable.removeMeta(key: String) {
    removeMetadata(key, plugin)
}
