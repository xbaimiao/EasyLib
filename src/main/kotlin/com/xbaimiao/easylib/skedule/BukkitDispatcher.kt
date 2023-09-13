package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(
    private val async: Boolean = false,
    private val scheduler: EasyScheduler
) : ExecutorCoroutineDispatcher(), Delay, Executor {

    private var closed = false

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

    override fun close() {
        closed = true
    }

    override val executor: Executor = Executor {
        if (async) {
            scheduler.runTaskAsynchronously(it)
        } else {
            scheduler.runTask(it)
        }
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!context.isActive || closed) {
            return
        }

        if (!async && Bukkit.isPrimaryThread()) {
            block.run()
        } else {
            runTask(block)
        }
    }

    override fun execute(command: java.lang.Runnable) {
        if (closed) {
            return
        }
        executor.execute(command)
    }

}

fun currentContext() = if (Bukkit.isPrimaryThread()) SynchronizationContext.SYNC else SynchronizationContext.ASYNC

val asyncDispatcher: ExecutorCoroutineDispatcher by lazy { BukkitDispatcher(true, EasyScheduler) }
val syncDispatcher: ExecutorCoroutineDispatcher by lazy { BukkitDispatcher(false, EasyScheduler) }

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
): Job {
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

    val dispatcher = if (initialContext == SynchronizationContext.SYNC) {
        syncDispatcher
    } else {
        asyncDispatcher
    }

    return controller.launch(dispatcher) {
        block.createCoroutine(receiver = controller, completion = controller).resume(Unit)
    }
}