package com.xbaimiao.easylib.database.dsl.wrapper

import com.j256.ormlite.field.DatabaseField
import com.xbaimiao.easylib.util.warn
import java.util.*
import kotlin.reflect.KProperty

private val columnNameCache = HashMap<String, String>()

fun <T> KProperty<T>.getColumnName(owner: Class<*>): String {
    val mapKey = "${owner.name}#${this.name}"
    if (columnNameCache.containsKey(mapKey)) {
        return columnNameCache[mapKey]!!
    }
    val field = owner.getDeclaredField(this.name).annotations.firstOrNull { it is DatabaseField }
    if (field != null) {
        val columnName = (field as DatabaseField).columnName
        if (columnName.isNotEmpty()) {
            columnNameCache[mapKey] = columnName
            return columnName
        }
    }
    warn("annotation not use columnName")
    return camelToSnake(this.name)
}

fun camelToSnake(name: String): String {
    val regex = Regex("([a-z0-9])([A-Z])")
    // 将大写字母与小写字母相连的位置插入下划线，并转换为小写
    return name.replace(regex) { matchResult ->
        "${matchResult.groups[1]!!.value}_${matchResult.groups[2]!!.value}"
    }.lowercase(Locale.getDefault())
}