package com.xbaimiao.easylib.expression

/**
 * @author 小白
 * @date 2023/5/15 15:44
 **/

class ExpressionResult(val result: Any) {

    companion object {
        private val TRUE = "true|yes|on".toRegex()
    }

    fun asBoolean(): Boolean {
        if (result is Boolean) return result
        return asString().lowercase().matches(TRUE)
    }

    fun asInt(): Int {
        if (result is Int) return result
        return asString().toInt()
    }

    fun asDouble(): Double {
        if (result is Double) return result
        return asString().toDouble()
    }

    fun asString(): String {
        if (result is String) return result
        return result.toString()
    }

    fun asFloat(): Float {
        if (result is Float) return result
        return asString().toFloat()
    }

}