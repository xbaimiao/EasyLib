package com.xbaimiao.easylib.database.dsl

import com.j256.ormlite.dao.Dao
import com.xbaimiao.easylib.database.DefaultMysqlConfiguration
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * @author xbaimiao
 * @date 2024/9/21
 * @email owner@xbaimiao.com
 */
private val daoMap = ConcurrentHashMap<String, Dao<*, *>>()

fun <T : Any, ID> KClass<T>.dao(): Dao<T, ID> {
    return daoMap.getOrPut(simpleName){
        DefaultMysqlConfiguration.ormlite.createDao<Dao<T, ID>, T>(java)
    } as Dao<T, ID>
}