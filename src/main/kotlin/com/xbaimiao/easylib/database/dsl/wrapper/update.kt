package com.xbaimiao.easylib.database.dsl.wrapper

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.UpdateBuilder
import com.j256.ormlite.stmt.Where
import kotlin.reflect.KProperty

/**
 * @author xbaimiao
 * @date 2024/10/10
 * @email owner@xbaimiao.com
 */
fun <T, ID> Dao<T, ID>.update(wrapper: UpdateBuilder<T, ID>.() -> Unit): Int {
    val updateBuilder = this.updateBuilder()
    wrapper.invoke(updateBuilder)
    return updateBuilder.update()
}

inline fun <reified T, ID> UpdateBuilder<T, ID>.where(wrapper: WhereWrapper<T, ID>.() -> Unit) {
    val where: Where<T, ID> = where()
    val whereWrapper = WhereWrapper(where, T::class.java)
    wrapper.invoke(whereWrapper)
    whereWrapper.build()
}

inline fun <reified T, ID> UpdateBuilder<T, ID>.update(column: KProperty<*>, value: Any) {
    this.updateColumnValue(column.getColumnName(T::class.java), value)
}