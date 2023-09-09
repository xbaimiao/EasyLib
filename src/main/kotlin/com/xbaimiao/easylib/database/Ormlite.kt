package com.xbaimiao.easylib.database

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource

interface Ormlite : SQLDatabase {

    val connectionSource: ConnectionSource

    fun <D : Dao<T, *>?, T> createDao(clazz: Class<T>?): D

}