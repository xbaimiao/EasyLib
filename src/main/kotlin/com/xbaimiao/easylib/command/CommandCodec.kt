package com.xbaimiao.easylib.command

interface CommandCodec<T> {

    fun name(): String

    fun encode(data: T): String

}
