package com.xbaimiao.easylib.bridge.economy

/**
 * @author 小白
 * @date 2023/5/31 09:43
 **/
object EconomyManager {

    val vault: Economy by lazy { Vault() }

    val playerPoints: Economy by lazy { PlayerPoints() }

    fun createGemsEco(gemsEconomyName: String): Economy {
        return GemsEco(gemsEconomyName)
    }

}