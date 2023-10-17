package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit

/**
 * EasyScheduler
 *
 * @author xbaimiao
 * @since 2023/9/12 21:07
 */
object EasyScheduler {

    fun runTaskLaterAsynchronously(runnable: Runnable, delay: Long): EasyLibTask {
        return submit(delay = delay, async = true) { runnable.run() }
    }

    fun runTaskLater(runnable: Runnable, delay: Long): EasyLibTask {
        return submit(delay = delay, async = false) { runnable.run() }
    }

    fun runTaskAsynchronously(runnable: Runnable): EasyLibTask {
        return submit(async = true) { runnable.run() }
    }

    fun runTask(runnable: Runnable): EasyLibTask {
        return submit(async = false) { runnable.run() }
    }

}