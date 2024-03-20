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
        return jedisPool.resource.use { conn ->
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
        jedisPool.resource.use { conn ->
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
