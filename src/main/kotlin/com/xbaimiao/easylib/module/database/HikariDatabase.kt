package com.xbaimiao.easylib.module.database

import com.xbaimiao.easylib.EasyPlugin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.configuration.ConfigurationSection

class HikariDatabase(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val user: String,
    private val passwd: String,
    private val ssl: Boolean
) {

    constructor(configuration: ConfigurationSection) : this(
        host = configuration.getString("host")!!,
        port = configuration.getInt("port"),
        database = configuration.getString("database")!!,
        user = configuration.getString("user")!!,
        passwd = configuration.getString("passwd")!!,
        ssl = configuration.getBoolean("ssl"),
    )

    val config: HikariConfig
    val dataSource: HikariDataSource

    init {
        val url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",
            host,
            port,
            database,
            ssl
        )
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

}