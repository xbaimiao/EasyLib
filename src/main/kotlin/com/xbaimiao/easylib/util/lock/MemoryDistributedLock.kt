package com.xbaimiao.easylib.util.lock

import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.currentContext
import com.xbaimiao.easylib.skedule.schedule
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * MemoryDistributedLock
 *
 * @author xbaimiao
 * @since 2023/10/14 12:10
 */
class MemoryDistributedLock : DistributedLock {

    private val lockMutexMap = HashMap<String, Mutex>()
    private val lockMutex = Mutex()

    override suspend fun <T> withLock(lockName: String, acquireTimeout: Long, timeout: Long, func: () -> T): T {
        val context = currentContext()
        val lockMutex = getLockMutex(lockName)
        return lockMutex.withLock {
            callContext(context, func)
        }
    }

    private suspend fun getLockMutex(lockName: String): Mutex {
        lockMutex.lock()
        try {
            return lockMutexMap.computeIfAbsent(lockName) { Mutex() }
        } finally {
            lockMutex.unlock()
        }
    }

    override suspend fun lockWithTimeout(lockName: String, acquireTimeout: Long, timeout: Long): String? {
        val lockMutex = getLockMutex(lockName)
        lockMutex.lock()
        return this::class.java.simpleName
    }

    override suspend fun unlock(lockName: String, identifier: String): Boolean {
        if (identifier != this::class.java.simpleName) {
            return false
        }
        val lockMutex = getLockMutex(lockName)
        lockMutex.unlock()
        return true
    }

    private suspend fun <T> callContext(context: SynchronizationContext, func: () -> T): T = suspendCoroutine {
        schedule(context) {
            it.resume(func.invoke())
        }
    }

}