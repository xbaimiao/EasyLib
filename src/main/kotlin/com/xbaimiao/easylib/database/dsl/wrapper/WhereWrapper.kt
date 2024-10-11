package com.xbaimiao.easylib.database.dsl.wrapper

import com.j256.ormlite.stmt.Where
import com.xbaimiao.easylib.util.debug
import kotlin.reflect.KProperty

/**
 * @author xbaimiao
 * @date 2024/10/10
 * @email owner@xbaimiao.com
 */
class WhereWrapper<T, ID>(val where: Where<T, ID>, val owner: Class<*>) {

    // 操作序列
    private val sequence = mutableListOf<Query<T>>()

    data class Query<T>(
        val query: Where<T, *>.() -> Unit,
        val type: QueryType = QueryType.LINK,
    )

    enum class QueryType {
        LINK, NO_LINK
    }

    private val KProperty<*>.columnName get() = getColumnName(owner)

    infix fun KProperty<*>.eq(value: Any) = sequence.add(
        Query({ eq(this@eq.columnName, value) })
    )

    infix fun KProperty<*>.`==`(value: Any) = sequence.add(
        Query({ eq(this@`==`.columnName, value) })
    )

    infix fun KProperty<*>.ne(value: Any) = sequence.add(
        Query({ ne(this@ne.columnName, value) })
    )

    infix fun KProperty<*>.`!=`(value: Any) = sequence.add(
        Query({ ne(this@`!=`.columnName, value) })
    )

    // between
    infix fun KProperty<*>.between(value: Pair<*, *>) = sequence.add(
        Query({ between(this@between.columnName, value.first, value.second) })
    )

    infix fun KProperty<*>.between(value: ClosedRange<*>) = sequence.add(
        Query({ between(this@between.columnName, value.start, value.endInclusive) })
    )

    // ge >=
    infix fun KProperty<*>.ge(value: Any) = sequence.add(
        Query({ ge(this@ge.columnName, value) })
    )

    // gt >
    infix fun KProperty<*>.gt(value: Any) = sequence.add(
        Query({ gt(this@gt.columnName, value) })
    )

    // in
    infix fun KProperty<*>.`in`(value: Collection<*>) = sequence.add(
        Query({ `in`(this@`in`.columnName, value) })
    )

    // notIn
    infix fun KProperty<*>.notIn(value: Collection<*>) = sequence.add(
        Query({ notIn(this@notIn.columnName, value) })
    )

    // isNull
    fun KProperty<*>.isNull() = sequence.add(
        Query({ isNull(this@isNull.columnName) })
    )

    // isNotNull
    fun KProperty<*>.isNotNull() = sequence.add(
        Query({ isNotNull(this@isNotNull.columnName) })
    )

    // le <=
    infix fun KProperty<*>.le(value: Any) = sequence.add(
        Query({ le(this@le.columnName, value) })
    )

    // lt <
    infix fun KProperty<*>.lt(value: Any) = sequence.add(
        Query({ lt(this@lt.columnName, value) })
    )

    // like
    infix fun KProperty<*>.like(value: Any) = sequence.add(
        Query({ like(this@like.columnName, value) })
    )

    // not
    fun KProperty<*>.not() = sequence.add(
        Query({ not() }, QueryType.NO_LINK)
    )

    // and
    fun and() = sequence.add(
        Query({ and() }, QueryType.NO_LINK)
    )

    // or
    fun or() = sequence.add(
        Query({ or() }, QueryType.NO_LINK)
    )

    fun build() {
        // 除了第一个以外都自动填充 AND
        // 如果下一个元素是 NO_LINK 则不填充
        sequence.forEachIndexed { index, it ->
            // 如果是NO_LINK 直接invoke
            if (it.type == QueryType.NO_LINK) {
                it.query.invoke(where)
                return@forEachIndexed
            }
            // 如果是第一个元素 或者他上一个元素不是NO_LINK  自动and
            if (index == 0 || sequence.getOrNull(index - 1)?.type != QueryType.NO_LINK) {
                if (index > 0) {
                    // 不是第一次 并且上一个不是NO_LINK 则默认为and
                    where.and()
                }
                // 第一次 直接 invoke
                it.query.invoke(where)
            } else {
                it.query.invoke(where)
            }
        }
        debug("statement ${where.statement}")
    }

}