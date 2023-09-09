package com.xbaimiao.easylib.database

import org.bukkit.configuration.ConfigurationSection

/**
 * MysqlHikariDatabase
 *
 * @author xbaimiao
 * @since 2023/8/19 17:13
 */
class MysqlHikariDatabase(
    host: String,
    port: Int,
    database: String,
    user: String,
    passwd: String,
    ssl: Boolean
) : HikariDatabase(
    String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true",
        host,
        port,
        database,
        ssl
    ), user, passwd
) {

    constructor(configuration: ConfigurationSection) : this(
        host = configuration.getString("host")!!,
        port = configuration.getInt("port"),
        database = configuration.getString("database")!!,
        user = configuration.getString("user")!!,
        passwd = configuration.getString("passwd")!!,
        ssl = configuration.getBoolean("ssl"),
    )

}