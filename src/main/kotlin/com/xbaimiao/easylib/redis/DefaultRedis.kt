package com.xbaimiao.easylib.redis

import com.xbaimiao.easylib.util.plugin
import org.bukkit.configuration.file.YamlConfiguration
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.io.File

/**
 * DefaultRedis
 *
 * @author xbaimiao
 * @since 2023/11/8 00:32
 */
object DefaultRedis {

    @JvmStatic
    private val init by lazy {
        val file = File(plugin.dataFolder, "redis.yml")
        if (file.exists()) {
            return@lazy YamlConfiguration.loadConfiguration(file)
        }
        if (!file.absoluteFile.parentFile.exists()) {
            file.absoluteFile.parentFile.mkdirs()
        }
        file.createNewFile()
        file.writeText(
            """
            host: localhost
            port: 6379
                #  password: ""
        """.trimIndent(), Charsets.UTF_8
        )
        return@lazy YamlConfiguration.loadConfiguration(file)
    }

    @JvmStatic
    val jedisPool by lazy {
        val host = init.getString("host")
        val port = init.getInt("port")
        val password = init.getString("password")
        val config = JedisPoolConfig()
        config.maxTotal = 10
        val jedisPool = if (password != null) {
            JedisPool(config, host, port, 5000, password)
        } else {
            JedisPool(config, host, port, 5000)
        }
        jedisPool.resource.use { it.get("1") }
        jedisPool
    }

}
