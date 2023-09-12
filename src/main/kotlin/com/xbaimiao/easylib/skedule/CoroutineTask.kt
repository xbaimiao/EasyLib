package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import kotlin.coroutines.resume

class CoroutineTask internal constructor(private val controller: SchedulerController) {

    val currentTask: EasyLibTask?
        get() = controller.currentTask
    val isSync: Boolean
        get() = controller.currentTask?.isSync ?: false
    val isAsync: Boolean
        get() = !(controller.currentTask?.isSync ?: true)

    fun cancel() {
        controller.resume(Unit)
    }

}