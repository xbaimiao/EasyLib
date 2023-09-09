package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val file: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(val node: String)

fun loadConfig(configObj: Any) {
    val configClass = configObj::class.java
    val configFileAnnotation = configClass.getAnnotation(Config::class.java)
        ?: error("${configObj::class.java.simpleName} must have @Config annotation")

    val file = File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, configFileAnnotation.file)

    val configFields = configClass.declaredFields.filter {
        it.isAnnotationPresent(ConfigNode::class.java)
    }

    val configuration = YamlConfiguration.loadConfiguration(file)

    var isChange = false

    for (field in configFields) {
        field.isAccessible = true
        val annotation = field.getAnnotation(ConfigNode::class.java)
        val yamlValue = configuration.get(annotation.node)
        if (yamlValue == null) {
            info("config.yml not found ${annotation.node}. auto create")
            configuration.set(annotation.node, field.get(configObj))
            if (!isChange) {
                isChange = true
            }
            continue
        }
        info("config.yml found ${annotation.node}. value: $yamlValue")
        field.set(configObj, yamlValue)
    }
    if (isChange) {
        configuration.save(file)
    }

}


