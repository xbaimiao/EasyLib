package com.xbaimiao.easylib.command

interface CommandCodec<T> {

    fun encode(data: T): String

}
