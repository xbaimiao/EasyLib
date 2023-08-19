package com.xbaimiao.easylib.module.database

import com.xbaimiao.easylib.EasyPlugin
import java.io.File

/**
 * SQLiteHikariDatabase
 *
 * @author xbaimiao
 * @since 2023/8/19 17:15
 */
class SQLiteHikariDatabase(url: String, user: String?, passwd: String?) : HikariDatabase(url, user, passwd) {

    constructor(file: File, user: String?, passwd: String?) : this("jdbc:sqlite:${file.path}", user, passwd)

    constructor(file: File) : this(file, null, null)

    constructor(name: String) : this(File(EasyPlugin.getPlugin<EasyPlugin>().dataFolder, name))

}