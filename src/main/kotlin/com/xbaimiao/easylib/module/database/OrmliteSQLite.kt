package com.xbaimiao.easylib.module.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.Level
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import com.xbaimiao.easylib.EasyPlugin
import java.io.File

class OrmliteSQLite(private val name: String) : Ormlite {

    override val connectionSource: ConnectionSource

    init {
        connectionSource = getConnectionSource()
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.WARNING)
    }

    @JvmName("getConnectionSource1")
    private fun getConnectionSource(): ConnectionSource {
        val url = "jdbc:sqlite:" + File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, name).path
        return JdbcConnectionSource(url)
    }

    override fun <D : Dao<T, *>?, T> createDao(clazz: Class<T>?): D {
        val dao: Dao<T, *> = DaoManager.createDao(connectionSource, clazz)
        if (!dao.isTableExists) {
            TableUtils.createTable(connectionSource, clazz)
        }
        return DaoManager.createDao(connectionSource, clazz)
    }
}