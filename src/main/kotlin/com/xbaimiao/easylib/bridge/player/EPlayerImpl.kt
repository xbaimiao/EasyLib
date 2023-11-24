package com.xbaimiao.easylib.bridge.player

import com.cryptomorin.xseries.messages.ActionBar
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author 小白
 * @date 2023/5/31 10:43
 **/
class EPlayerImpl(private val player: Player) : EPlayer {

    override fun sendActionBar(string: String) {
        ActionBar.sendActionBar(player, string)
    }

    override fun playSound(sound: String) {
        sound.parseToESound().playSound(player)
    }

    override fun execCommands(list: List<String>, sender: CommandSender) {
        list.parseECommand(player).exec(sender)
    }

    override fun fakeOperator(): CommandSender {
        return FakeOperator(player)
    }

    companion object {
        fun Player.easylib(): EPlayer {
            return EPlayerImpl(this)
        }
    }

}