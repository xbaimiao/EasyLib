package com.xbaimiao.easylib.database

import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.ConfigurationSection
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
    @JvmOverloads
    fun newOrInit(fileName: String = "database.yml"): ConfigurationSection {
        val file = File(plugin.dataFolder, fileName)
        if (file.exists()) {
            return YamlConfiguration.loadConfiguration(file)
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()

        val configuration = YamlConfiguration.loadConfiguration(file)
        configuration.set("host", "localhost")
        configuration.set("port", 3306)
        configuration.set("database", "minecraft")
        configuration.set("user", "minecraft")
        configuration.set("passwd", "minecraft")
        configuration.set("ssl", false)

        configuration.save(file)

        return YamlConfiguration.loadConfiguration(file)
    }

}