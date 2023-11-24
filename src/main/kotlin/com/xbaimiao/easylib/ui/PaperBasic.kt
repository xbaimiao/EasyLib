package com.xbaimiao.easylib.ui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * PaperBasic
 *
 * @author xbaimiao
 * @since 2023/10/18 17:36
 */
class PaperBasic(player: Player, val title: Component) : Basic(player) {

    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(holderCallback(this), if (rows > 0) rows * 9 else slots.size * 9, title)
        handleInventory(inventory)
        return inventory
    }

}