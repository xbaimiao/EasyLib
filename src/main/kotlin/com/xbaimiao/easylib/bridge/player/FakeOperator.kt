package com.xbaimiao.easylib.bridge.player

import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissibleBase
import org.bukkit.permissions.Permission
import java.util.*

class FakeOperator(val player: Player, ) : PermissibleBase(player), CommandSender {


    override fun hasPermission(inName: String): Boolean {
        return true
    }

    override fun hasPermission(perm: Permission): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }


    override fun sendMessage(vararg messages: String) {
        player.sendMessage(messages)
    }


    override fun sendMessage(sender: UUID?, message: String) {
        player.sendMessage(sender, message)
    }


    override fun sendMessage(sender: UUID?, vararg messages: String) {
        player.sendMessage(sender, messages)
    }

    override fun getServer(): Server {
        return player.server
    }


    override fun getName(): String {
        return player.name
    }

    override fun spigot(): CommandSender.Spigot {
        return player.spigot()
    }

}