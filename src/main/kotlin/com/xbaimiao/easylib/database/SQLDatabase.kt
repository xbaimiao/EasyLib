package com.xbaimiao.easylib.database

import java.sql.Connection

/**
 * SQLDatabase
 *
 * @author xbaimiao
 * @since 2023/8/19 17:02
 */
interface SQLDatabase {

    /**
     * 使用一个链接，链接会自动释放
     */
    fun <T> useConnection(block: (Connection) -> T): T

    /**
     * 使用事务操作mysql
     */
    fun <T> transaction(func: (Connection) -> T): T

}
