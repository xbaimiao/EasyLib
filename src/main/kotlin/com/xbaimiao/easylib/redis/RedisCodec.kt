package com.xbaimiao.easylib.redis

import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams

data class RedisCodec<T>(
    val jedisPool: JedisPool,
    val dataCodec: DataCodec<T>,
) {

    fun set(key: String, value: T) {
        jedisPool.resource.use { conn ->
            conn.set(key, dataCodec.encode(value))
        }
    }

    fun set(key: String, value: T, setParams: SetParams) {
        jedisPool.resource.use { conn ->
            conn.set(key, dataCodec.encode(value), setParams)
        }
    }

    fun get(key: String): T? {
        return jedisPool.resource.use { conn ->
            val data = conn.get(key) ?: return null
            return@use dataCodec.decode(data)
        }
    }

}
