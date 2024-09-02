package com.xbaimiao.easylib.bridge.economy

import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class Vault : Economy<Double> {

    private lateinit var economy: net.milkbowl.vault.economy.Economy

    init {
        val rsp =
            Bukkit.getServer().servicesManager.getRegistration(net.milkbowl.vault.economy.Economy::class.java)
        if (rsp == null) {
            info("Vault挂钩失败")
        } else {
            economy = rsp.provider
        }
    }

    override fun take(player: OfflinePlayer, amount: Double) {
        if (!this::economy.isInitialized) {
            warn("Vault挂钩失败")
            return
        }
        economy.withdrawPlayer(player, amount)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        if (!this::economy.isInitialized) {
            warn("Vault挂钩失败")
            return false
        }
        return economy.has(player, amount)
    }

    override fun get(player: OfflinePlayer): Double {
        if (!this::economy.isInitialized) {
            warn("Vault挂钩失败")
            return 0.0
        }
        return economy.getBalance(player)
    }

    override fun give(player: OfflinePlayer, amount: Double) {
        if (!this::economy.isInitialized) {
            warn("Vault挂钩失败")
            return
        }
        economy.depositPlayer(player, amount)
    }

    override fun set(player: OfflinePlayer, amount: Double) {
        val has = get(player)
        if (has > amount) {
            take(player, has - amount)
        } else {
            give(player, amount - has)
        }
    }

}
