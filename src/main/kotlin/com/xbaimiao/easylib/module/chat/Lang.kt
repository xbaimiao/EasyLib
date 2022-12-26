package com.xbaimiao.easylib.module.chat

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.colored
import com.xbaimiao.easylib.module.utils.info
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Suppress("unused")
object Lang {

    private var configuration: YamlConfiguration? = null
    private var jarLang: YamlConfiguration? = null
    private var file: File? = null
    private var init = false

    private fun init(plugin: JavaPlugin) {
        if (init) {
            return
        }
        val inputStream = plugin.getResource("lang.yml")
        if (inputStream == null) {
            info("lang.yml not found")
            return
        }
        jarLang =
            YamlConfiguration.loadConfiguration(BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        file = File(plugin.dataFolder, "lang.yml")
        if (!file!!.exists()) {
            try {
                plugin.saveResource("lang.yml", false)
            } catch (e: IllegalArgumentException) {
                info("lang.yml not found")
                return
            }
        }

        configuration = YamlConfiguration.loadConfiguration(file!!)
        init = true
    }

    private fun save() {
        configuration!!.save(file!!)
    }

    fun CommandSender.sendLang(path: String, vararg args: Any) {
        init(EasyPlugin.getPlugin())
        var obj = configuration!![path]
        if (obj == null && jarLang != null) {
            obj = jarLang!![path]
            configuration!![path] = obj
            save()
        }
        if (obj is List<*>) {
            obj.forEach { msg ->
                this.sendMessage((msg as String?).colored().formats(*args))
            }
        } else {
            this.sendMessage((obj as String?).colored().formats(*args))
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> asLangText(path: String, vararg args: Any): T {
        init(EasyPlugin.getPlugin())
        var obj = configuration!![path]
        if (obj == null && jarLang != null) {
            obj = jarLang!![path]
            configuration!![path] = obj
            save()
        }
        return if (obj is List<*>) {
            obj.map { (it as String?).colored().formats(*args) }.toList() as T
        } else {
            (obj as String?).colored().formats(*args) as T
        }
    }

    private fun String.formats(vararg args: Any): String {
        var index = 0
        var result = this
        args.forEach {
            result = result.replace("{$index}", it.toString())
            index++
        }
        return result
    }

}