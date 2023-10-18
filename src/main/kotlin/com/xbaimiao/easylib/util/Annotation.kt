package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.EasyPlugin
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EConfig(val file: String)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(val node: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EListener(val depend: Array<String> = [])

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class EPlaceholderExpansion

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ECommandHeader(
    val command: String,
    val permission: String = "",
    val permissionMessage: String = "",
    val description: String = "",
    val debug: Boolean = false
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody




