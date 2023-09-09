package com.xbaimiao.easylib.command

/**
 * @author 小白
 * @date 2023/3/27 18:35
 **/
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandHeader(
    val command: String,
    val permission: String = "",
    val permissionMessage: String = "",
    val description: String = ""
)

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandBody

