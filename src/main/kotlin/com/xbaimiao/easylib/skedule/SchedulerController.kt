package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.*

class SchedulerController : Continuation<Unit>, CoroutineScope by CoroutineScope(EmptyCoroutineContext) {

    override val context: CoroutineContext = EmptyCoroutineContext

    private var currentTask: EasyLibTask? = null

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
        submit(delay = ticks, async = currentContext().isAsync) {
            cont.resume(ticks)
        }
    }

    /**
     * 异步运行一段代码，此方法等同于Bukkit runTaskAsynchronously 方法 但会挂起函数 等待结果后回到原来的线程执行
     */
    suspend fun <T> async(asyncFunc: () -> T): T = suspendCoroutine { cont ->
        val context = currentContext()
        // 如果本来就是异步的 直接执行
        if (context.isAsync) {
            cont.resume(asyncFunc())
            return@suspendCoroutine
        }
        // 不是异步的 先异步执行获取结果 再回到原来的线程执行
        currentTask = submit(async = true) {
            val result = asyncFunc()
            currentTask = submit {
                cont.resume(result)
            }
        }
    }

    /**
     * 同步运行一段代码，此方法等同于Bukkit runTask 方法 但会挂起函数 等待结果后回到原来的线程执行
     */
    suspend fun <T> sync(syncFunc: () -> T): T = suspendCoroutine { cont ->
        val context = currentContext()
        // 如果本来就是同步的 直接执行
        if (context.isAsync.not()) {
            cont.resume(syncFunc())
            return@suspendCoroutine
        }
        // 不是同步的 先同步执行获取结果 再回到原来的线程执行
        currentTask = submit(async = false) {
            val result = syncFunc()
            currentTask = submit(async = context.isAsync) {
                cont.resume(result)
            }
        }
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
        if (currentContext().isAsync == context.isAsync) {
            cont.resume(false)
            return@suspendCoroutine
        }
        submit(async = context.isAsync) {
            cont.resume(true)
        }
    }

}