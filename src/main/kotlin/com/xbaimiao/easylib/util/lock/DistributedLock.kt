package com.xbaimiao.easylib.util.lock

import redis.clients.jedis.JedisPool

/**
 * DistributedLock
 *
 * @author xbaimiao
 * @since 2023/10/14 12:08
 */
interface DistributedLock {

    /**
     * 使用分布式锁执行一段代码
     */
    suspend fun <T> withLock(lockName: String, acquireTimeout: Long = 10000, timeout: Long = 10000, func: () -> T): T

    /**
     * 加锁
     *
     * @param lockName 锁的key
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间
     * @return 锁标识
     */
    suspend fun lockWithTimeout(lockName: String, acquireTimeout: Long, timeout: Long): String?

    /**
     * 释放锁
     *
     * @param lockName 锁的key
     * @param identifier 释放锁的标识
     * @return 是否释放成功
     */
    suspend fun unlock(lockName: String, identifier: String): Boolean

}

fun buildDistributedLock(jedisPool: JedisPool): DistributedLock {
    return RedisDistributedLock(jedisPool)
}

fun buildDistributedLock(): DistributedLock {
    return MemoryDistributedLock()
}