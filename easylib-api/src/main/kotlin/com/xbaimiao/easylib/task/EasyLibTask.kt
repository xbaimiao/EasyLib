package com.xbaimiao.easylib.task

/**
 * @author 小白
 * @date 2023/4/18 10:17
 **/
interface EasyLibTask : Runnable {

    fun runTask()

    fun runTaskLater(delay: Long)

    fun runTaskTimer(delay: Long, period: Long)

    fun runTaskAsynchronously()

    fun runTaskLaterAsynchronously(delay: Long)

    fun runTaskTimerAsynchronously(delay: Long, period: Long)

    fun cancel()

    fun isCancelled(): Boolean

}