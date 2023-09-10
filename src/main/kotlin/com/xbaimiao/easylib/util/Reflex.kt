package com.xbaimiao.easylib.util

/**
 * 检查这个类是否是另一个类的父类
 */
fun Class<*>.isSuperClassOf(clazz: Class<*>): Boolean {
    if (this.isAssignableFrom(clazz)) {
        return true
    }
    return this == clazz
}