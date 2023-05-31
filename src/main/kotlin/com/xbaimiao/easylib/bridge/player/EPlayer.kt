package com.xbaimiao.easylib.bridge.player

import org.bukkit.command.CommandSender

/**
 * @author 小白
 * @date 2023/5/31 10:42
 **/
interface EPlayer {

    /**
     * 发送ActionBar信息
     */
    fun sendActionBar(string: String)

    /**
     * 对玩家播放音效
     */
    fun playSound(sound: String)

    /**
     * 对此玩家执行命令 会自动替换%player_name% 和 papi变量为此玩家的
     */
    fun execCommands(list: List<String>, sender: CommandSender)

}