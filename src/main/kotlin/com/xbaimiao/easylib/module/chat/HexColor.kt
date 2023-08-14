package com.xbaimiao.easylib.module.chat

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import java.util.*


/**
 * @author sky
 * @since 2021/1/18 2:02 下午
 */
object HexColor {

    private var isLegacy = false

    init {
        try {
            ChatColor.of(Color.BLACK)
        } catch (ignored: NoSuchMethodError) {
            isLegacy = true
        }
    }

    /**
     * 对字符串中的特殊颜色表达式进行转换<br></br>
     * 可供转换的格式有：
     *
     *
     * &amp;{255-255-255} —— RGB 代码
     *
     *
     * &amp;{255,255,255} —— RGB 代码
     *
     *
     * &amp;{#FFFFFF}     —— HEX 代码
     *
     *
     * &amp;{BLUE}        —— 已知颜色（英文）
     *
     *
     * &amp;{蓝}          —— 已知颜色（中文）
     *
     * @param in 字符串
     * @return String
     */
    fun translate(`in`: String): String {
        if (isLegacy) {
            return ChatColor.translateAlternateColorCodes('&', `in`)
        }
        val builder = StringBuilder()
        val chars = `in`.toCharArray()
        var i = 0
        while (i < chars.size) {
            if (i + 1 < chars.size && chars[i] == '&' && chars[i + 1] == '{') {
                var chatColor: ChatColor? = null
                var match = CharArray(0)
                var j = i + 2
                while (j < chars.size && chars[j] != '}') {
                    match = arrayAppend(match, chars[j])
                    j++
                }
                if (match.size == 11 && (match[3] == ',' || match[3] == '-') && (match[7] == ',' || match[7] == '-')) {
                    chatColor = ChatColor.of(Color(toInt(match, 0, 3), toInt(match, 4, 7), toInt(match, 8, 11)))
                } else if (match.size == 7 && match[0] == '#') {
                    try {
                        chatColor = ChatColor.of(toString(match))
                    } catch (ignored: IllegalArgumentException) {
                    }
                } else {
                    val knownColor: Optional<StandardColors> = StandardColors.match(toString(match))
                    if (knownColor.isPresent) {
                        chatColor = knownColor.get().toChatColor()
                    }
                }
                if (chatColor != null) {
                    builder.append(chatColor)
                    i += match.size + 2
                }
            } else {
                builder.append(chars[i])
            }
            i++
        }
        return ChatColor.translateAlternateColorCodes('&', builder.toString())
    }

    fun getColorCode(color: Int): String {
        return ChatColor.of(Color(color)).toString()
    }

    private fun arrayAppend(chars: CharArray, `in`: Char): CharArray {
        val newChars = CharArray(chars.size + 1)
        System.arraycopy(chars, 0, newChars, 0, chars.size)
        newChars[chars.size] = `in`
        return newChars
    }

    private fun toString(chars: CharArray): String {
        val builder = StringBuilder()
        for (c in chars) {
            builder.append(c)
        }
        return builder.toString()
    }

    private fun toInt(chars: CharArray, start: Int, end: Int): Int {
        val builder = StringBuilder()
        for (i in start until end) {
            builder.append(chars[i])
        }
        return builder.toString().toInt()
    }
}

