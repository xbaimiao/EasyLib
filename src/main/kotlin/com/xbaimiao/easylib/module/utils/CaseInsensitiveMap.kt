package com.xbaimiao.easylib.module.utils

/**
 * CaseInsensitiveMap
 *
 * @author xbaimiao
 * @since 2023/8/12 22:33
 */
class CaseInsensitiveMap<V> : HashMap<String, V>() {

    override fun containsKey(key: String): Boolean {
        return super.keys.any { it.equals(key, true) }
    }

    override fun get(key: String): V? {
        return super.keys.firstOrNull { it.equals(key, true) }?.let { super.get(it) }
    }

}