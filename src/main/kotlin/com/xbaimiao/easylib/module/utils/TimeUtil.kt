package com.xbaimiao.easylib.module.utils

/**
 * @author 小白
 * @date 2023/3/27 15:36
 **/
object TimeUtil {

    /**
     * 将 1d 1h 1s 转为毫秒
     */
    fun analyze(argument: String): Long {
        // 毫秒
        if (argument.endsWith("ms")){
            return argument.substring(0, argument.length - 1).toLong()
        }
        // 秒
        if (argument.endsWith("s")) {
            return argument.substring(0, argument.length - 1).toLong() * 1000L
        }
        // 分钟
        if (argument.endsWith("m")){
            return argument.substring(0, argument.length - 1).toLong() * 60000L
        }
        // 小时
        if (argument.endsWith("h")) {
            return argument.substring(0, argument.length - 1).toLong() * 3600000L
        }
        // 天
        if (argument.endsWith("d")) {
            return argument.substring(0, argument.length - 1).toLong() * 86400000L
        }
        return argument.toLongOrNull() ?: 0L
    }

    /**
     * 将毫秒转为 xx时xx分xx秒
     */
    fun formatTime(time: Long): String {
        val day = time / 86400000
        val hour = time % 86400000 / 3600000
        val minute = time % 3600000 / 60000
        val second = time % 60000 / 1000
        return if (day > 0) {
            "${day}天${hour}时${minute}分${second}秒"
        } else if (hour > 0) {
            "${hour}时${minute}分${second}秒"
        } else if (minute > 0) {
            "${minute}分${second}秒"
        } else {
            "${second}秒"
        }
    }

}