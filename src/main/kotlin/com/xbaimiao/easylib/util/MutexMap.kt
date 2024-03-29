package com.xbaimiao.easylib.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

/**
 * MutexMap
 *
 * @author xbaimiao
 * @since 2023/11/13 17:32
 */
object MutexMap {

    private val mutexMap = WeakHashMap<String, Mutex>()
    private val mutex = Mutex()

    suspend fun getMutex(key: String): Mutex {
        return mutex.withLock {
            mutexMap.computeIfAbsent(key) { Mutex() }
        }
    }

    suspend fun removeMutex(key: String) {
        mutex.withLock {
            mutexMap.remove(key)
        }
    }

}
