package com.xbaimiao.easylib.database

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
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.jdbcUrl = url
        config.username = user
        config.password = passwd

        config.minimumIdle = 2
        config.maximumPoolSize = 10
        config.idleTimeout = 600000
        config.maxLifetime = 1800000
        config.isAutoCommit = true
        config.connectionTimeout = 30000
        config.validationTimeout = 5000
        config.keepaliveTime = 30000

        dataSource = HikariDataSource(config)
    }

    override fun <T> useConnection(block: (Connection) -> T): T {
        return dataSource.connection.use { block(it) }
    }

    override fun <T> transaction(func: (Connection) -> T): T {
        return useConnection {
            var isSuccessful = false
            it.autoCommit = false
            try {
                val result = func(it)
                isSuccessful = true
                result
            } finally {
                if (isSuccessful) {
                    it.commit()
                } else {
                    it.rollback()
                }
                it.autoCommit = true
            }
        }
    }

}
