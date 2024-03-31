package com.xbaimiao.easylib.util

import java.lang.reflect.Method

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

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DependencyList(val depends: Array<Dependency>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dependency(
    val url: String,
    val clazz: String,
    val format: Boolean = false,
    val repoUrl: String = "https://maven.aliyun.com/repository/public/",
    val relocationRules: Array<String> = [],
    val fetchDependencies: Boolean = false
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AwakeClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val lifeCycle: LifeCycle)

class LifeCycleMethod(val lifeCycle: LifeCycle, val method: Method, val instance: Any)

enum class LifeCycle {
    ENABLE, ACTIVE, DISABLE
}


