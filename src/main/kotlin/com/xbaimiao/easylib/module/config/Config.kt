package com.xbaimiao.easylib.module.config

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(val file: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(val node: String)

fun loadConfig(configObj: Any) {
    val configClass = configObj::class.java
    val configFileAnnotation = configClass.getAnnotation(Config::class.java)
        ?: error("${configObj::class.java.simpleName} must have @Config annotation")

    val configFile = File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, configFileAnnotation.file)

    val configProperties = configClass.declaredFields.filter { it.isAnnotationPresent(ConfigNode::class.java) }

    val yamlConfig = YamlConfiguration.loadConfiguration(configFile)

    for (property in configProperties) {
        property.isAccessible = true
        val annotation = property.getAnnotation(ConfigNode::class.java)
        val propertyName = annotation.node
        val propertyValue = yamlConfig.get(propertyName) ?: property.get(configObj)
        property.set(configObj, propertyValue)
    }
}


