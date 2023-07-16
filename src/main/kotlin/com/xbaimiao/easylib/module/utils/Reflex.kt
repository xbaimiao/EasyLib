package com.xbaimiao.easylib.module.utils

import java.lang.reflect.Field

/**
 * 检查这个类是否是另一个类的父类
 */
fun Class<*>.isSuperClassOf(clazz: Class<*>): Boolean {
    if (this.isAssignableFrom(clazz)) {
        return true
    }
    return this == clazz
}

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
@Throws(Exception::class)
fun <T> Any.getProperty(fieldName: String, deep: Int = 8): T? {
    val field = this.javaClass.getFields(fieldName, deep) ?: return null
    field.isAccessible = true
    return field[this] as T
}

/**
 * 设置一个属性为对应的值
 */
@Throws(Exception::class)
fun Any.setProperty(fieldName: String, value: Any, deep: Int = 8) {
    val field = this.javaClass.getFields(fieldName, deep) ?: return
    field.isAccessible = true
    field[this] = value
}

/**
 * 获取一个方法的返回值
 */
@Suppress("Unchecked_cast")
@Throws(Exception::class)
fun <T> Any.invokeMethod(methodName: String, vararg args: Any): T? {
    val classes = args.map { it.javaClass }.toTypedArray()
    val method = this.javaClass.getDeclaredMethod(methodName, *classes)
    method.isAccessible = true
    return method.invoke(this, *args) as T
}