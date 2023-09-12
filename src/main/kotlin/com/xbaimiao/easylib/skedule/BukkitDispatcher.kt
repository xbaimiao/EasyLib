package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(
    private val async: Boolean = false,
    private val scheduler: EasyScheduler
) : CoroutineDispatcher(), Delay {

    private val runTaskLater: (Runnable, Long) -> EasyLibTask =
        if (async)
            scheduler::runTaskLaterAsynchronously
        else
            scheduler::runTaskLater
    private val runTask: (Runnable) -> EasyLibTask =
        if (async)
            scheduler::runTaskAsynchronously
        else
            scheduler::runTask

    @ExperimentalCoroutinesApi
    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val task = runTaskLater(
            Runnable {
                continuation.apply { resumeUndispatched(Unit) }
            },
            timeMillis / 50
        )
        continuation.invokeOnCancellation { task.cancel() }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive) {
            return
        }

        if (!async && Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            runTask(block)
        }
    }

}

fun currentContext() = if (Bukkit.isPrimaryThread()) SynchronizationContext.SYNC else SynchronizationContext.ASYNC

fun dispatcher(async: Boolean = false) = BukkitDispatcher(async, EasyScheduler)

/**
 * Schedule a coroutine with the Bukkit Scheduler.
 *
 * @receiver The BukkitScheduler instance to use for scheduling tasks.
 * @param initialContext The initial synchronization context to start off the coroutine with. See
 * [SynchronizationContext].
 *
 * @see SynchronizationContext
 */
fun schedule(
    initialContext: SynchronizationContext = SynchronizationContext.SYNC,
    co: suspend SchedulerController.() -> Unit
): CoroutineTask {
    val controller = SchedulerController(EasyScheduler)

    val block: suspend SchedulerController.() -> Unit = {
        try {
            // 切换线程
            start(initialContext)
            // 执行代码
            co()
        } finally {
            cleanup()
        }
    }

    block.createCoroutine(receiver = controller, completion = controller).resume(Unit)

    return CoroutineTask(controller)
}