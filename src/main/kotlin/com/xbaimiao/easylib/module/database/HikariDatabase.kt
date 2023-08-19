package com.xbaimiao.easylib.module.database

import com.xbaimiao.easylib.EasyPlugin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

abstract class HikariDatabase(url: String, user: String?, passwd: String?) : SQLDatabase {

    val config: HikariConfig
    val dataSource: HikariDataSource

    init {
        config = HikariConfig()
        config.poolName = EasyPlugin.getPlugin<EasyPlugin>().name + "-MySQLConnectionPool"
        config.minimumIdle = 4
        config.maximumPoolSize = 16
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.jdbcUrl = url
        config.username = user
        config.password = passwd
        config.maxLifetime = 60000
        dataSource = HikariDataSource(config)
    }

    override fun useConnection(block: (Connection) -> Unit) {
        dataSource.connection.use {
            block(it)
        }
    }

}