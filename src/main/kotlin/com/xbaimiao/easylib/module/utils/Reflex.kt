package com.xbaimiao.easylib.module.utils

import java.lang.reflect.Field


private fun Class<*>.getFields(fieldName: String, deep: Int, currentDeep: Int = 0): Field? {
    if (currentDeep > deep) {
        return null
    }
    return try {
        val field = this.getDeclaredField(fieldName)
        field.apply { isAccessible = true }
    } catch (ex: NoSuchFieldException) {
        try {
            if (this.javaClass.superclass == null) {
                return null
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            return null
        }
        this.javaClass.superclass.getFields(fieldName, deep, currentDeep + 1)
    }
}


/**
 * 获取这个字段的值
 */
@Suppress("Unchecked_cast")
fun <T> Any.getProperty(fieldName: String, deep: Int = 8): T? {
    try {
        val field = this.javaClass.getFields(fieldName, deep) ?: return null
        field.isAccessible = true
        return field[this] as T
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    }
    return null
}

/**
 * 设置一个属性为对应的值
 */
fun Any.setProperty(fieldName: String, value: Any, deep: Int = 8) {
    try {
        val field = this.javaClass.getFields(fieldName, deep) ?: return
        field.isAccessible = true
        field[this] = value
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    }
}

/**
 * 获取一个方法的返回值
 */
@Suppress("Unchecked_cast")
fun <T> Any.invokeMethod(methodName: String, vararg args: Any): T? {
    try {
        val classes = args.map { it.javaClass }.toTypedArray()
        val method = this.javaClass.getDeclaredMethod(methodName, *classes)
        method.isAccessible = true
        return method.invoke(this, *args) as T
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}