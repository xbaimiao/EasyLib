package com.xbaimiao.easylib.module.config

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * @author 小白
 * @date 2023/4/9 22:25
 **/
interface AutoFileConfiguration {

    val file: File

    fun load() {
        load(this, javaClass)
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T : AutoFileConfiguration> load(instance: AutoFileConfiguration, clazz: Class<T>): T {

            val data = clazz.declaredFields
                .asSequence()
                .filter { it.isAnnotationPresent(ConfigNode::class.java) }
                .associateWith { it.getAnnotation(ConfigNode::class.java)!! }


            val configuration = YamlConfiguration.loadConfiguration(instance.file)

            data.forEach { (field, annotation) ->
                val value = configuration.getCast(field.type, annotation.path)
                if (value != null) {
                    runCatching {
                        field.isAccessible = true
                        field.set(instance, value)
                    }.onFailure {
                        error("无法读取ConfigNode ${field.name} 请检查配置文件或联系开发者")
                    }
                } else {
                    error("无法读取ConfigNode ${annotation.path} 不存在 ")
                }
            }

            return instance as T
        }

        private fun <T> ConfigurationSection.getCast(clazz: Class<T>, path: String): Any? {
            if (clazz == Boolean::class.java || clazz.isAssignableFrom(Boolean::class.java)) {
                return getBoolean(path)
            }
            if (clazz == Int::class.java || clazz.isAssignableFrom(Int::class.java)) {
                return getInt(path)
            }
            if (clazz == Long::class.java || clazz.isAssignableFrom(Long::class.java)) {
                return getLong(path)
            }
            if (clazz == Double::class.java || clazz.isAssignableFrom(Double::class.java)) {
                return getDouble(path)
            }
            if (clazz == String::class.java || clazz.isAssignableFrom(String::class.java)) {
                return getString(path)
            }
            if (clazz == List::class.java || clazz.isAssignableFrom(List::class.java)) {
                return getList(path)
            }
            if (clazz == ConfigurationSection::class.java || clazz.isAssignableFrom(ConfigurationSection::class.java)) {
                return getConfigurationSection(path)
            }
            return when {
                isBoolean(path) -> getBoolean(path)
                isInt(path) -> getInt(path)
                isLong(path) -> getLong(path)
                isDouble(path) -> getDouble(path)
                isString(path) -> getString(path)
                isList(path) -> getList(path)
                isConfigurationSection(path) -> getConfigurationSection(path)
                else -> null
            }
        }

    }

}