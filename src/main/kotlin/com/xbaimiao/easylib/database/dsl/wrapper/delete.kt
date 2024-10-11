package com.xbaimiao.easylib.database.dsl.wrapper

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.DeleteBuilder
import com.j256.ormlite.stmt.Where

/**
 * @author xbaimiao
 * @date 2024/10/10
 * @email owner@xbaimiao.com
 */
fun <T, ID> Dao<T, ID>.delete(wrapper: DeleteBuilder<T, ID>.() -> Unit): Int {
    val deleteBuilder = this.deleteBuilder()
    wrapper.invoke(deleteBuilder)
    return deleteBuilder.delete()
}

inline fun <reified T, ID> DeleteBuilder<T, ID>.where(wrapper: WhereWrapper<T, ID>.() -> Unit) {
    val where: Where<T, ID> = where()
    val whereWrapper = WhereWrapper(where, T::class.java)
    wrapper.invoke(whereWrapper)
    whereWrapper.build()
}

