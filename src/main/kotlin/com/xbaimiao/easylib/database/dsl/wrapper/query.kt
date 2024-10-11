package com.xbaimiao.easylib.database.dsl.wrapper

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.stmt.QueryBuilder
import com.j256.ormlite.stmt.Where

fun <T, ID> Dao<T, ID>.query(wrapper: QueryBuilder<T, ID>.() -> Unit): T? {
    val queryBuilder = this.queryBuilder()
    wrapper.invoke(queryBuilder)
    return queryBuilder.queryForFirst()
}

fun <T, ID> Dao<T, ID>.queryList(wrapper: QueryBuilder<T, ID>.() -> Unit): MutableList<T> {
    val queryBuilder = this.queryBuilder()
    wrapper.invoke(queryBuilder)
    return queryBuilder.query() ?: mutableListOf()
}

inline fun <reified T, ID> Dao<T, ID>.countOf(wrapper: WhereWrapper<T, ID>.() -> Unit): Long {
    val queryBuilder = this.queryBuilder()
    val where: Where<T, ID> = queryBuilder.where()
    // 构成Where包装器
    val whereWrapper = WhereWrapper(where, T::class.java)
    wrapper.invoke(whereWrapper)
    // build并应用于where
    whereWrapper.build()
    return where.countOf()
}

inline fun <reified T, ID> QueryBuilder<T, ID>.select(wrapper: WhereWrapper<T, ID>.() -> Unit) {
    val where = where()
    // 构成Where包装器
    val whereWrapper = WhereWrapper<T, ID>(where, T::class.java)
    wrapper.invoke(whereWrapper)
    // build并应用于where
    whereWrapper.build()
}

inline fun <reified T, ID> Dao<T, ID>.select(crossinline wrapper: WhereWrapper<T, ID>.() -> Unit): T? {
    return query { select(wrapper) }
}

inline fun <reified T, ID> Dao<T, ID>.selectList(crossinline wrapper: WhereWrapper<T, ID>.() -> Unit): MutableList<T> {
    return queryList { select(wrapper) }
}
