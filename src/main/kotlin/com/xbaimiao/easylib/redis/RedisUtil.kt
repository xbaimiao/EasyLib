package com.xbaimiao.easylib.redis

import redis.clients.jedis.JedisPool

fun <T> JedisPool.codec(dataCodec: DataCodec<T>) = RedisCodec(this, dataCodec)
