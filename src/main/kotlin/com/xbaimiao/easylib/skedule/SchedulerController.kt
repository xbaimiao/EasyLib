package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.*

class SchedulerController(
    scheduler: EasyScheduler
) : Continuation<Unit>, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    private var schedulerDelegate: TaskScheduler = NonRepeatingTaskScheduler(scheduler)

    val currentTask: EasyLibTask?
        get() = schedulerDelegate.currentTask

    internal suspend fun start(initialContext: SynchronizationContext) = suspendCoroutine { cont ->
        schedulerDelegate.doContextSwitch(initialContext) { cont.resume(Unit) }
    }

    internal fun cleanup() {
        currentTask?.cancel()
    }

    override fun resumeWith(result: Result<Unit>) {
        cleanup()
        result.getOrThrow()
    }

    /**
     * 等待多少tick后继续执行
     *
     * @return 等待的tick数
     */
    suspend fun waitFor(ticks: Long): Long = suspendCoroutine { cont ->
        schedulerDelegate.doWait(ticks, cont::resume)
    }

    /**
     * 异步运行一段代码，此方法等同于Bukkit runTaskAsynchronously 方法 但会挂起函数 等待结果后回到原来的线程执行
     */
    suspend fun <T> async(asyncFunc: () -> T): T = suspendCoroutine { cont ->
        schedulerDelegate.doAsync(asyncFunc) { cont.resume(it) }
    }

    /**
     * 同步运行一段代码，此方法等同于Bukkit runTask 方法 但会挂起函数 等待结果后回到原来的线程执行
     */
    suspend fun <T> sync(syncFunc: () -> T): T = suspendCoroutine { cont ->
        schedulerDelegate.doSync(syncFunc) { cont.resume(it) }
    }

    /**
     * 启动协作程序运行一段代码 此方法等同于Bukkit runTaskAsynchronously 方法 但他不会等待结果而是还会接着往下运行 除非你调用 [Deferred.await] 方法
     */
    fun <T> runAsync(asyncFunc: suspend SchedulerController.() -> T): Deferred<T> {
        return async(asyncDispatcher) {
            asyncFunc(this@SchedulerController)
        }
    }

    suspend fun <T> callAsync(asyncFunc: suspend SchedulerController.() -> T): T {
        return runAsync(asyncFunc).await()
    }

    /**
     * 启动协作程序运行一段代码 此方法等同于Bukkit runTask 方法 但他不会等待结果而是还会接着往下运行 除非你调用 [Deferred.await] 方法
     */
    fun <T> runSync(syncFunc: suspend SchedulerController.() -> T): Deferred<T> {
        return async(syncDispatcher) {
            syncFunc(this@SchedulerController)
        }
    }

    suspend fun <T> callSync(syncFunc: suspend SchedulerController.() -> T): T {
        return runSync(syncFunc).await()
    }

    /**
     * 切换到指定的SynchronizationContext 如果这个协程已经在给定的上下文中 那么这个方法什么也不做并立即返回
     *
     * @param context 要切换到的 SynchronizationContext
     * @return 如果进行了上下文切换，则为true，否则为false
     */
    suspend fun switchContext(context: SynchronizationContext): Boolean = suspendCoroutine { cont ->
        schedulerDelegate.doContextSwitch(context, cont::resume)
    }

    /**
     * 强制在指定的上下文中安排新任务。此方法将导致计划新的重复或非重复任务。重复状态和解决方案由当前运行的currentTask决定
     *
     * @param context 新任务的同步 SynchronizationContext
     */
    suspend fun newContext(context: SynchronizationContext): Unit = suspendCoroutine { cont ->
        schedulerDelegate.forceNewContext(context) { cont.resume(Unit) }
    }

}