package com.xbaimiao.easylib.module.config

/**
 * @author 小白
 * @date 2023/4/9 22:21
 **/

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigNode(
    val path: String,
    val onlyRead: Boolean = false
)
