package com.xbaimiao.easylib.database.player

/**
 * Database
 *
 * @author xbaimiao
 * @since 2023/8/19 16:31
 */
interface Database {

    fun getMap(user: String): HashMap<String, String>

    operator fun set(user: String, namespace: String, value: String)

    operator fun get(user: String, namespace: String): String?

}