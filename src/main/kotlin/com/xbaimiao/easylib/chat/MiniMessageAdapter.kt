package com.xbaimiao.easylib.chat

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * @author xbaimiao
 * @date 2024/10/10
 * @email owner@xbaimiao.com
 */
object MiniMessageAdapter {

    private val mm by lazy {
        kotlin.runCatching { MiniMessage.miniMessage() }.getOrNull()
    }

    fun translate(string: String): Component? {
        return mm?.deserialize(string)
    }

}