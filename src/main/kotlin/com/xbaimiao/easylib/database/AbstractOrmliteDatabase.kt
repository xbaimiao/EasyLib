package com.xbaimiao.easylib.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.table.TableUtils
import java.sql.Connection

/**
 * AbstractOrmliteDatabase
 *
 * @author xbaimiao
 * @since 2023/8/19 17:10
 */
abstract class AbstractOrmliteDatabase : Ormlite {

    override fun <D : Dao<T, *>?, T> createDao(clazz: Class<T>?): D {
        val dao: Dao<T, *> = DaoManager.createDao(connectionSource, clazz)
        if (!dao.isTableExists) {
            TableUtils.createTable(connectionSource, clazz)
        }
        return DaoManager.createDao(connectionSource, clazz)
    }

    override fun <T> useConnection(block: (Connection) -> T): T {
        val connection = connectionSource.getReadOnlyConnection("")
        return block(connection.underlyingConnection).also { connectionSource.releaseConnection(connection) }
    }

}