package com.xbaimiao.easylib.database

import com.xbaimiao.easylib.EasyPlugin
import java.io.File
import java.sql.Connection
import java.util.concurrent.locks.ReentrantLock

/**
 * SQLiteHikariDatabase
 *
 * @author xbaimiao
 * @since 2023/8/19 17:15
 */
class SQLiteHikariDatabase(url: String, user: String?, passwd: String?) : HikariDatabase(url, user, passwd) {

    constructor(file: File, user: String?, passwd: String?) : this("jdbc:sqlite:${file.path}", user, passwd)

    constructor(file: File) : this(file, null, null)

    constructor(name: String) : this(File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, name))

    private val transactionLock = ReentrantLock()

    override fun <T> transaction(func: (Connection) -> T): T {
        transactionLock.lock()
        try {
            return super.transaction(func)
        } finally {
            transactionLock.unlock()
        }
    }

}
