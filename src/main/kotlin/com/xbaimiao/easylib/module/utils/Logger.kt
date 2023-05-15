@file:JvmName("Logger")

package com.xbaimiao.easylib.module.utils

import org.bukkit.plugin.java.JavaPlugin

fun printStackTrace() {
    println(
        "当前线程: ${Thread.currentThread().name}\r\n${
            Thread.currentThread().stackTrace.drop(2).joinToString("\r\n") { "        at $it" }
        }"
    )
}

fun printStackTrace(e: Throwable) {
    println(
        "当前线程: ${Thread.currentThread().name}\r\n$e\r\n${
            Thread.currentThread().stackTrace.drop(2).joinToString("\r\n") { "        at $it" }
        }"
    )
}

fun printStackTrace(plugin: JavaPlugin) {
    println(
        "Print stack trace from plugin ${plugin.name} v${plugin.description.version}\r\n${
            Thread.currentThread().stackTrace.drop(
                2
            ).joinToString("\r\n") { "        at $it" }
        }"
    )
}

fun printStackTrace(plugin: JavaPlugin, e: Throwable) {
    println(
        "Print stack trace from plugin ${plugin.name} v${plugin.description.version}\r\n$e\r\n${
            Thread.currentThread().stackTrace.drop(
                2
            ).joinToString("\r\n") { "        at $it" }
        }"
    )
}