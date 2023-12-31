package com.xbaimiao.easylib.util

import redis.clients.jedis.JedisPool
import redis.clients.jedis.Transaction
import java.util.*

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
    fun <T> withLock(lockName: String, acquireTimeout: Long = 10000, timeout: Long = 10000, func: () -> T): T

    /**
     * 加锁
     *
     * @param lockName 锁的key
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间
     * @return 锁标识
     */
    fun lockWithTimeout(lockName: String, acquireTimeout: Long, timeout: Long): String?

    /**
     * 释放锁
     *
     * @param lockName 锁的key
     * @param identifier 释放锁的标识
     * @return 是否释放成功
     */
    fun unlock(lockName: String, identifier: String): Boolean

}

class MemoryDistributedLock : DistributedLock {

    // 定义一个全局的锁对象，用于控制锁的互斥
    private val lock = Any()

    // 定义一个Map，用于存储锁的标识和锁的超时时间
    private val locks = mutableMapOf<String, Pair<String, Long>>()

    override fun <T> withLock(lockName: String, acquireTimeout: Long, timeout: Long, func: () -> T): T {
        // 获取锁的超时时间，超过这个时间则放弃获取锁
        val end = System.currentTimeMillis() + acquireTimeout
        while (System.currentTimeMillis() < end) {
            synchronized(lock) {
                // 尝试获取锁
                val pair = locks[lockName]
                if (pair == null) {
                    // 如果锁不存在，则创建锁
                    val identifier = UUID.randomUUID().toString()
                    locks[lockName] = Pair(identifier, System.currentTimeMillis() + timeout)
                    try {
                        // 执行加锁代码块
                        return func.invoke()
                    } finally {
                        // 释放锁
                        synchronized(lock) {
                            locks.remove(lockName)
                        }
                    }
                } else if (pair.second < System.currentTimeMillis()) {
                    // 如果锁已经超时，则删除锁
                    synchronized(lock) {
                        locks.remove(lockName)
                    }
                }
            }
            Thread.sleep(10)
        }
        // 加锁超时，抛出异常
        throw RuntimeException("加锁超时，请尝试增加 acquireTimeout")
    }

    override fun lockWithTimeout(lockName: String, acquireTimeout: Long, timeout: Long): String? {
        // 获取锁的超时时间，超过这个时间则放弃获取锁
        val end = System.currentTimeMillis() + acquireTimeout
        while (System.currentTimeMillis() < end) {
            synchronized(lock) {
                // 尝试获取锁
                val pair = locks[lockName]
                if (pair == null) {
                    // 如果锁不存在，则创建锁
                    val identifier = UUID.randomUUID().toString()
                    locks[lockName] = Pair(identifier, System.currentTimeMillis() + timeout)
                    return identifier
                } else if (pair.second < System.currentTimeMillis()) {
                    // 如果锁已经超时，则删除锁
                    locks.remove(lockName)
                }
            }
            Thread.sleep(10)
        }
        // 加锁超时，返回null
        return null
    }

    override fun unlock(lockName: String, identifier: String): Boolean {
        synchronized(lock) {
            // 判断锁是否存在
            val pair = locks[lockName]
            if (pair != null && pair.first == identifier) {
                // 如果锁存在且标识匹配，则删除锁
                locks.remove(lockName)
                return true
            }
        }
        // 锁不存在或标识不匹配，返回false
        return false
    }

}

class RedisDistributedLock(private val jedisPool: JedisPool) : DistributedLock {

    override fun <T> withLock(lockName: String, acquireTimeout: Long, timeout: Long, func: () -> T): T {
        if (lockName.contains(":")) {
            error("lockName 中不能包含 \":\"")
        }
        val identifier =
            lockWithTimeout(lockName, acquireTimeout, timeout) ?: error("加锁超时 请尝试增加 acquireTimeout")
        val result = func.invoke()
        unlock(lockName, identifier)
        return result
    }

    override fun lockWithTimeout(lockName: String, acquireTimeout: Long, timeout: Long): String? {
        return jedisPool.getResource().use { conn ->
            // 随机生成一个value
            val identifier = UUID.randomUUID().toString()
            // 锁名，即key值
            val lockKey = "lock:$lockName"
            // 超时时间，上锁后超过此时间则自动释放锁
            val lockExpire = (timeout / 1000)

            // 获取锁的超时时间，超过这个时间则放弃获取锁
            val end = System.currentTimeMillis() + acquireTimeout
            while (System.currentTimeMillis() < end) {
                if (conn.setnx(lockKey, identifier) == 1L) {
                    conn.expire(lockKey, lockExpire)
                    // 返回value值，用于释放锁时间确认
                    return@use identifier
                }
                // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                if (conn.ttl(lockKey) == -1L) {
                    conn.expire(lockKey, lockExpire)
                }
                Thread.sleep(10)
            }
            // 返回结果
            return@use null
        }
    }

    override fun unlock(lockName: String, identifier: String): Boolean {
        jedisPool.getResource().use { conn ->
            val lockKey = "lock:$lockName"
            var retFlag = false
            while (true) {
                // 监视lock，准备开始事务
                conn.watch(lockKey)
                // 通过前面返回的value值判断是不是该锁，若是该锁，则删除，释放锁
                if (identifier == conn.get(lockKey)) {
                    val transaction: Transaction = conn.multi()
                    transaction.del(lockKey)
                    val results = transaction.exec()
                    if (results == null) {
                        Thread.sleep(10)
                        continue
                    }
                    retFlag = true
                }
                conn.unwatch()
                break
            }
            return retFlag
        }
    }

}

fun buildDistributedLock(jedisPool: JedisPool): DistributedLock {
    return RedisDistributedLock(jedisPool)
}

fun buildDistributedLock(): DistributedLock {
    return MemoryDistributedLock()
}
