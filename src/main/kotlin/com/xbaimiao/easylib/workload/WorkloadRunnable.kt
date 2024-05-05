package com.xbaimiao.easylib.workload

import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit
import org.bukkit.Bukkit
import java.util.*

/**
 * 这是WorkloadRunnable的基本实现。它在给定字段 max_milliseconds per_tick 允许的情况下，每个节拍处理尽可能多的工作负载。
 */
class WorkloadRunnable : Runnable {

    private val workloadDeque: Deque<Workload> = ArrayDeque()
    private val lock = Any()
    private lateinit var task: EasyLibTask
    private var isStart = false

    fun addWorkload(workload: Workload) {
        synchronized(lock) {
            workloadDeque.add(workload)
        }
    }

    override fun run() {
        synchronized(lock) {
            if (!Bukkit.isPrimaryThread()) {
                error("WorkloadRunnable must run in main thread")
            }
            if (workloadDeque.isEmpty()) {
                return
            }
            val stopTime = System.currentTimeMillis() + (MAX_PERIOD_TICK * 50)
            var nextLoad = workloadDeque.poll()
            while (System.currentTimeMillis() <= stopTime && nextLoad != null) {
                nextLoad.compute()
                nextLoad = workloadDeque.poll()
            }
        }
    }

    fun start() {
        if (isStart) {
            error("WorkloadRunnable has already started")
        }
        isStart = true
        task = submit(period = PERIOD_TICK) {
            run()
        }
    }

    fun stop() {
        if (!isStart) {
            error("WorkloadRunnable has not started")
        }
        task.cancel()
    }

    companion object {
        // 1 tick 50 毫秒
        private const val PERIOD_TICK = 1L

        // 20 tick 1 秒
        private const val MAX_PERIOD_TICK = (PERIOD_TICK * 20)
    }

}
