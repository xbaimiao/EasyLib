package com.xbaimiao.easylib.bridge.economy

import me.xanium.gemseconomy.api.GemsEconomyAPI
import me.xanium.gemseconomy.currency.Currency
import org.bukkit.OfflinePlayer

class GemsEco(
    private val gemsEconomyName: String
) : Economy {

    private val api: GemsEconomyAPI by lazy { GemsEconomyAPI() }
    private val currency: Currency by lazy { api.getCurrency(gemsEconomyName) }

    override fun take(player: OfflinePlayer, amount: Double) {
        api.withdraw(player.uniqueId, amount, currency)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        val balance: Double = api.getBalance(player.uniqueId, currency)
        return amount <= balance
    }

    override fun get(player: OfflinePlayer): Double {
        return api.getBalance(player.uniqueId, currency)
    }

    override fun give(player: OfflinePlayer, amount: Double) {
        api.deposit(player.uniqueId, amount, currency)
    }

}