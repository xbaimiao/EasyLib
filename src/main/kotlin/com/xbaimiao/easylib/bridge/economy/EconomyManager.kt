package com.xbaimiao.easylib.bridge.economy

/**
 * @author 小白
 * @date 2023/5/31 09:43
 **/
object EconomyManager {

    val vault: Economy<Double> by lazy { Vault() }

    val playerPoints: Economy<Int> by lazy { PlayerPoints() }

    fun createGemsEco(gemsEconomyName: String): Economy<Double> {
        return GemsEco(gemsEconomyName)
    }

}
