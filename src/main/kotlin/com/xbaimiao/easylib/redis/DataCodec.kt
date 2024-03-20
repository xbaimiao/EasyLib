package com.xbaimiao.easylib.redis

interface DataCodec<T> {

    fun encode(data: T): String

    fun decode(data: String): T

}
