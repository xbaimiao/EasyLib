package com.xbaimiao.easylib.database

import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.logger.Level
import com.j256.ormlite.support.ConnectionSource
import com.xbaimiao.easylib.EasyPlugin
import java.io.File

class OrmliteSQLite(private val name: String) : AbstractOrmliteDatabase() {

    override val connectionSource: ConnectionSource by lazy {
        val url = "jdbc:sqlite:" + File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, name).path
        JdbcConnectionSource(url)
    }

    init {
        com.j256.ormlite.logger.Logger.setGlobalLogLevel(Level.WARNING)
    }

}