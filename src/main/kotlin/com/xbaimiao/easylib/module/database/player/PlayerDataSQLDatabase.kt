package com.xbaimiao.easylib.module.database.player

import com.xbaimiao.easylib.module.database.SQLDatabase
import com.xbaimiao.easylib.module.utils.debug
import com.xbaimiao.easylib.module.utils.info

/**
 * @author xbaimiao
 * @since 2023/8/19 16:33
 */
class PlayerDataSQLDatabase(private val sqlDatabase: SQLDatabase, private val tableName: String) : Database {

    init {
        createTable()
    }

    private fun createTable() {
        sqlDatabase.useConnection { connection ->
            connection.createStatement().use {
                it.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS $tableName (" +
                            "playerUUID VARCHAR(36) NOT NULL, " +
                            "namespace VARCHAR(255) NOT NULL, " +
                            "value LONGBLOB NOT NULL, " +
                            "PRIMARY KEY (playerUUID, namespace));"
                )
                info("Successfully created table $tableName")
            }
        }
    }

    override fun getMap(user: String): HashMap<String, String> {
        return sqlDatabase.useConnection { connection ->
            connection.prepareStatement("SELECT namespace, value FROM $tableName WHERE playerUUID = ?;")
                .use {
                    it.setString(1, user)
                    it.executeQuery().use { resultSet ->
                        val map = HashMap<String, String>()
                        while (resultSet.next()) {
                            map[resultSet.getString("namespace")] = String(resultSet.getBytes("value"))
                        }
                        return@useConnection map
                    }
                }
        }
    }

    override fun set(user: String, namespace: String, value: String) {
        debug("set $user $namespace $value")
        sqlDatabase.useConnection { connection ->
            connection.prepareStatement("INSERT INTO $tableName (playerUUID, namespace, value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value = ?;")
                .use {
                    it.setString(1, user)
                    it.setString(2, namespace)
                    it.setBytes(3, value.toByteArray())
                    it.setBytes(4, value.toByteArray())
                    it.executeUpdate()
                    debug("set $user $namespace $value success")
                }
        }
    }

    override fun get(user: String, namespace: String): String? {
        return sqlDatabase.useConnection { connection ->
            connection.prepareStatement("SELECT value FROM $tableName WHERE playerUUID = ? AND namespace = ?;")
                .use {
                    it.setString(1, user)
                    it.setString(2, namespace)
                    it.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            return@useConnection String(resultSet.getBytes("value"))
                        } else {
                            return@useConnection null
                        }
                    }
                }
        }
    }

}