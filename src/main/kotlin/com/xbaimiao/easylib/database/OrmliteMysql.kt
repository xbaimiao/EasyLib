package com.xbaimiao.easylib.database

import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.Level
import com.j256.ormlite.support.ConnectionSource
import org.bukkit.configuration.ConfigurationSection

class OrmliteMysql(
    private val host: String,
    private val port: Int,
    private val database: String,
    private val user: String,
    private val passwd: String,
    private val ssl: Boolean,
    private val hikariCP: Boolean
) : AbstractOrmliteDatabase() {

    override val connectionSource: ConnectionSource by lazy {
        val url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",
            host,
            port,
            database,
            ssl
        )
        if (hikariCP) {
            DataSourceConnectionSource(MysqlHikariDatabase(host, port, database, user, passwd, ssl).dataSource, url)
        } else {
            JdbcConnectionSource(url, user, passwd)
        }
    }

    init {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.WARNING)
    }

    constructor(configuration: ConfigurationSection, hikariCP: Boolean) : this(
        host = configuration.getString("host")!!,
        port = configuration.getInt("port"),
        database = configuration.getString("database")!!,
        user = configuration.getString("user")!!,
        passwd = configuration.getString("passwd")!!,
        ssl = configuration.getBoolean("ssl"),
        hikariCP = hikariCP
    )

    constructor(hikariCP: Boolean) : this(
        configuration = DefaultMysqlConfiguration.newOrInit(),
        hikariCP = hikariCP
    )

}