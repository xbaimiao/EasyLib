package com.xbaimiao.easylib.bridge.player

import com.xbaimiao.easylib.xseries.XSound
import org.bukkit.Sound

/**
 * @author 小白
 * @date 2023/5/19 10:14
 **/

fun String.parseToESound(): ESound {
    val xSound = runCatching {
        XSound.valueOf(this.uppercase())
    }.getOrNull()
    if (xSound != null) {
        return buildESound(xSound).build()
    }
    val sound = runCatching {
        Sound.valueOf(this.uppercase())
    }.getOrNull()
    if (sound != null) {
        return buildESound(sound).build()
    }
    return buildESound(this).build()
}

fun buildESound(sound: Sound): ESoundBuilder {
    return ESoundBuilder().also { it.sound = sound }
}

fun buildESound(xSound: XSound): ESoundBuilder {
    return ESoundBuilder().also { it.xSound = xSound }
}

fun buildESound(rwaSound: String): ESoundBuilder {
    return ESoundBuilder().also { it.rwaSound = rwaSound }
}

class ESoundBuilder {

    var sound: Sound? = null
    var xSound: XSound? = null
    var rwaSound: String? = null
    var volume: Float = 100f
    var pitch: Float = 1f

    fun build(): ESound {
        return ESound(sound, xSound, rwaSound, volume, pitch)
    }

}
