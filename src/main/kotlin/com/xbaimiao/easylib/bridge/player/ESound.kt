package com.xbaimiao.easylib.bridge.player

import com.cryptomorin.xseries.XSound
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author 小白
 * @date 2023/5/19 10:14
 **/
class ESound(
    private val sound: Sound? = null,
    private val xSound: XSound? = null,
    private val rwaSound: String? = null,
    var volume: Float = 100f,
    var pitch: Float = 1f
) {

    fun playSound(player: Player) {
        if (sound != null) {
            player.playSound(player.location, sound, volume, pitch)
        }
        xSound?.play(player, volume, pitch)
        rwaSound?.let {
            player.playSound(player.location, it, volume, pitch)
        }
    }

    fun playSound(location: Location) {
        if (sound != null) {
            location.world.playSound(location, sound, volume, pitch)
        }
        xSound?.play(location, volume, pitch)
        rwaSound?.let {
            location.world.playSound(location, it, volume, pitch)
        }
    }

    fun stopSound(player: Player) {
        if (sound != null) {
            player.stopSound(sound)
        }
        xSound?.stopSound(player)
        rwaSound?.let {
            player.stopSound(it)
        }
    }

}