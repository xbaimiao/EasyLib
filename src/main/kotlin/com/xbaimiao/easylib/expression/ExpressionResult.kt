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
        return asString().lowercase().matches(TRUE)
    }

    fun asInt(): Int {
        return asString().toInt()
    }

    fun asDouble(): Double {
        return asString().toDouble()
    }

    fun asString(): String {
        return result.toString()
    }

    fun asFloat(): Float {
        return asString().toFloat()
    }

}