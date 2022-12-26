package com.xbaimiao.easylib.module.chat

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.colored
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * jar内 内置配置文件 自动释放
 */
@Suppress("unused")
class BuiltInConfiguration(fileName: String) : YamlConfiguration() {

    val file: File

    init {
        file = File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, fileName)
        if (!file.exists()) {
            EasyPlugin.getPlugin<EasyPlugin>().saveResource(fileName, false)
        }
        super.load(file)
    }

    fun getStringColored(path: String): String {
        return super.getString(path).colored()
    }

    fun getStringListColored(path: String): List<String> {
        return super.getStringList(path).colored()
    }

    fun saveToFile() {
        super.save(file)
    }

}