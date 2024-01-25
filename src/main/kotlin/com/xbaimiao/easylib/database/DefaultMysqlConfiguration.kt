package com.xbaimiao.easylib.database

import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * DefaultMysqlConfiguration
 *
 * @author xbaimiao
 * @since 2023/11/8 00:32
 */
object DefaultMysqlConfiguration {

    @JvmStatic
    val init by lazy {
        val file = File(plugin.dataFolder, "database.yml")
        if (file.exists()) {
            return@lazy YamlConfiguration.loadConfiguration(file)
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()

        val configuration = YamlConfiguration.loadConfiguration(file)
        configuration.set("mysql", false)
        configuration.set("host", "localhost")
        configuration.set("port", 3306)
        configuration.set("database", "minecraft")
        configuration.set("user", "minecraft")
        configuration.set("passwd", "minecraft")
        configuration.set("ssl", false)

        configuration.save(file)

        return@lazy YamlConfiguration.loadConfiguration(file)
    }

    @JvmStatic
    val type by lazy {
        if (init.getBoolean("mysql")) {
            DatabaseType.MYSQL
        } else {
            DatabaseType.SQLITE
        }
    }

    @JvmStatic
    val hikari by lazy {
        if (type == DatabaseType.MYSQL) {
            MysqlHikariDatabase(init)
        } else {
            SQLiteHikariDatabase("database.db")
        }
    }

}
