package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask

interface TaskScheduler {

    val currentTask: EasyLibTask?

    fun doWait(ticks: Long, task: (Long) -> Unit)

    fun <T> doAsync(asyncFunc: () -> T, task: (T) -> Unit)

    fun <T> doSync(syncFunc: () -> T, task: (T) -> Unit)

    fun doContextSwitch(context: SynchronizationContext, task: (Boolean) -> Unit)

    fun forceNewContext(context: SynchronizationContext, task: () -> Unit)

}