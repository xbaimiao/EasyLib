package com.xbaimiao.easylib.workload

/**
 * WorkLoadRunnablePool
 */
class WorkLoadRunnablePool(size: Int) {

    private val list = ArrayList<WorkloadRunnable>()
    private var iterator: Iterator<WorkloadRunnable> = list.iterator()

    init {
        repeat(size) {
            val workloadRunnable = WorkloadRunnable()
            list.add(workloadRunnable)
        }
    }

    fun next(): WorkloadRunnable {
        if (!iterator.hasNext()) {
            iterator = list.iterator()
        }
        return iterator.next()
    }

    fun start() {
        list.forEach {
            it.start()
        }
    }

    fun stop() {
        list.forEach {
            it.stop()
        }
    }

}
