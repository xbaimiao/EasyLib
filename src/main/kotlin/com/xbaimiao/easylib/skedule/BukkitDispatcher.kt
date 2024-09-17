package com.xbaimiao.easylib.skedule

import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

@OptIn(InternalCoroutinesApi::class)
class BukkitDispatcher(
    private val async: Boolean,
    private val location: Location?,
    private val entity: Entity?,
) : ExecutorCoroutineDispatcher(), Delay, Executor {

    private var closed = false

    private val runTaskLater: (Runnable, Long) -> EasyLibTask = { runnable, delay ->
        submit(delay = delay, async = async, location = location, entity = entity) { runnable.run() }
    }
    private val runTask: (Runnable) -> EasyLibTask = { runnable ->
        submit(async = async, location = location, entity = entity) { runnable.run() }
    }

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

    @Deprecated(
        "Deprecated without replacement as an internal method never intended for public use",
        level = DeprecationLevel.ERROR
    )
    override suspend fun delay(time: Long) {
        if (time <= 0) {
            return
        }
        suspendCancellableCoroutine { cont ->
            val task = runTaskLater(Runnable { cont.resume(Unit) }, time / 50)
            cont.invokeOnCancellation { task.cancel() }
        }
    }

    override val executor: Executor = Executor {
        if (async) {
            if (currentContext() == SynchronizationContext.ASYNC) {
                it.run()
            } else {
                submit(async = true, location = location, entity = entity) {
                    it.run()
                }
            }
        } else {
            if (currentContext() == SynchronizationContext.SYNC) {
                it.run()
            } else {
                submit(location = location, entity = entity) { it.run() }
            }
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

/**
 * 启动一次协程。
 *
 * @receiver 用于计划任务的BukkitScheduler实例。
 * @param initialContext 用于启动协同程序的初始同步上下文
 * @see SynchronizationContext
 */
fun launchCoroutine(
    initialContext: SynchronizationContext = currentContext(),
    location: Location? = null,
    entity: Entity? = null,
    co: suspend SchedulerController.() -> Unit,
): Job {
    val asyncDispatcher = BukkitDispatcher(true, location, entity)
    val syncDispatcher = BukkitDispatcher(false, location, entity)
    val controller = SchedulerController(syncDispatcher, asyncDispatcher)

    val block: suspend SchedulerController.() -> Unit = {
        try {
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

@Deprecated(
    message = "Use launchCoroutine instead.",
    replaceWith = ReplaceWith("launchCoroutine(initialContext, co)"),
    level = DeprecationLevel.ERROR
)
fun schedule(
    initialContext: SynchronizationContext = SynchronizationContext.SYNC,
    co: suspend SchedulerController.() -> Unit,
): Job {
    return launchCoroutine(initialContext, null, null, co)
}
