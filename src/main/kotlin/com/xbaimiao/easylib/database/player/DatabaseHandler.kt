package com.xbaimiao.easylib.database.player

import com.xbaimiao.easylib.database.SQLDatabase
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * DatabaseHandler
 *
 * @author xbaimiao
 * @since 2023/8/19 17:58
 */
var playerDatabase: Database? = null

val playerDataContainer = ConcurrentHashMap<UUID, DataContainer>()

fun setupPlayerDatabase(sqlDatabase: SQLDatabase, tableName: String) {
    playerDatabase = PlayerDataSQLDatabase(sqlDatabase, tableName)
}

fun Player.getDataContainer(): DataContainer {
    return playerDataContainer[uniqueId] ?: error("unavailable")
}

fun Player.setupDataContainer(usernameMode: Boolean = false) {
    val user = if (usernameMode) name else uniqueId.toString()
    playerDataContainer[uniqueId] = DataContainer(user, playerDatabase!!)
}

fun Player.releaseDataContainer() {
    playerDataContainer.remove(uniqueId)
}