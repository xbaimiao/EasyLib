package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.launchCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * MutexMap
 *
 * @author xbaimiao
 * @since 2023/11/13 17:32
 */
object MutexMap : Listener {

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

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        launchCoroutine(SynchronizationContext.ASYNC) {
            removeMutex(event.player.name)
        }
    }

}