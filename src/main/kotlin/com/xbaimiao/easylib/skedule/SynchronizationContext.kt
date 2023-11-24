package com.xbaimiao.easylib.skedule

/**
 * 表示BukkitScheduler协同程序当前所在的同步上下文。
 */
enum class SynchronizationContext(val isAsync: Boolean) {

    /**
     * 协同程序处于同步上下文中，所有任务都安排在主服务器线程上同步执行。
     */
    SYNC(false),

    /**
     * 协同程序处于异步上下文中，所有任务都异步调度到主服务器线程。
     */
    ASYNC(true)

}