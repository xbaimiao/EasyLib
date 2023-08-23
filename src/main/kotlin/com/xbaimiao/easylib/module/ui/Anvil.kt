package com.xbaimiao.easylib.module.ui

import com.xbaimiao.easylib.bridge.replacePlaceholder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class Anvil(player: Player, title: String) : Basic(player, title) {

    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(holderCallback(this), InventoryType.ANVIL, title.replacePlaceholder(player))
        val line = slots.getOrNull(0)
        if (line != null) {
            var cel = 0
            while (cel < line.size && cel < 3) {
                inventory.setItem(cel, items[line[cel]] ?: ItemStack(Material.AIR))
                cel++
            }
        }
        slotItems.forEach { (k, v) ->
            inventory.setItem(k, v)
        }
        return inventory
    }

}