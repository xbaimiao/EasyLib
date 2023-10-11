package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.skedule.SynchronizationContext
import com.xbaimiao.easylib.skedule.currentContext
import com.xbaimiao.easylib.skedule.schedule
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Transaction
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DistributedLock(private val jedisPool: JedisPool, val lockName: String) {

    init {
        if (lockName.contains(":")) {
            error("lockName 中不能包含 \":\"")
        }
    }

    /**
     * 使用分布式锁执行一段代码
     */
    @JvmOverloads
    suspend fun exec(acquireTimeout: Long = 10000, timeout: Long = 10000, func: () -> Unit) {
        val identifier = lockWithTimeout(acquireTimeout, timeout) ?: error("加锁超时 请尝试增加 acquireTimeout")
        func.invoke()
        releaseLock(identifier)
    }

    /**
     * 加锁
     *
     * @param lockName       锁的key
     * @param acquireTimeout 获取超时时间
     * @param timeout        锁的超时时间
     * @return 锁标识
     */
    suspend fun lockWithTimeout(acquireTimeout: Long, timeout: Long): String? = suspendCoroutine {
        // 当前线程
        val context = currentContext()
        // 启用异步线程
        schedule(SynchronizationContext.ASYNC) {
            jedisPool.getResource().use { conn ->
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
                        // 切换回 context 当前线程
                        switchContext(context)
                        // 返回结果
                        it.resume(identifier)
                        return@use
                    }
                    // 返回-1代表key没有设置超时时间，为key设置一个超时时间
                    if (conn.ttl(lockKey) == -1L) {
                        conn.expire(lockKey, lockExpire)
                    }
                    waitFor(1)
                }
                // 切换回 context 当前线程
                switchContext(context)
                // 返回结果
                it.resume(null)
            }
        }
    }

    /**
     * 释放锁
     *
     * @param lockName   锁的key
     * @param identifier 释放锁的标识
     * @return 是否释放成功
     */
    suspend fun releaseLock(identifier: String): Boolean = suspendCoroutine {
        val context = currentContext()
        schedule(SynchronizationContext.ASYNC) {
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
                            waitFor(1)
                            continue
                        }
                        retFlag = true
                    }
                    conn.unwatch()
                    break
                }
                switchContext(context)
                it.resume(retFlag)
            }
        }
    }

}
