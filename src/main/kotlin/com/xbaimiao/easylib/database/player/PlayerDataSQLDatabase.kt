package com.xbaimiao.easylib.database.player

import com.xbaimiao.easylib.database.SQLDatabase
import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.info

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
            if (has(user, namespace)) {
                // update
                connection.prepareStatement("UPDATE $tableName SET value = ? WHERE playerUUID = ? AND namespace = ?;")
                    .use {
                        it.setBytes(1, value.toByteArray())
                        it.setString(2, user)
                        it.setString(3, namespace)
                        it.executeUpdate()
                        debug("set $user $namespace $value success")
                    }
            } else {
                // insert
                connection.prepareStatement("INSERT INTO $tableName (playerUUID, namespace, value) VALUES (?, ?, ?);")
                    .use {
                        it.setString(1, user)
                        it.setString(2, namespace)
                        it.setBytes(3, value.toByteArray())
                        it.executeUpdate()
                        debug("set $user $namespace $value success")
                    }
            }
        }
    }

    private fun has(user: String, namespace: String): Boolean {
        return sqlDatabase.useConnection { connection ->
            connection.prepareStatement("SELECT value FROM $tableName WHERE playerUUID = ? AND namespace = ?;")
                .use {
                    it.setString(1, user)
                    it.setString(2, namespace)
                    it.executeQuery().use { resultSet ->
                        return@useConnection resultSet.next()
                    }
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