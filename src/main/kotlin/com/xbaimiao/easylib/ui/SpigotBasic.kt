package com.xbaimiao.easylib.ui

import com.xbaimiao.easylib.bridge.replacePlaceholder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/**
 * SpigotBasic
 *
 * @author xbaimiao
 * @since 2023/10/18 17:35
 */
class SpigotBasic(player: Player, val title: String) : Basic(player) {

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        val inventory = Bukkit.createInventory(
            holderCallback(this),
            if (rows > 0) rows * 9 else slots.size * 9,
            title.replacePlaceholder(player)
        )
        handleInventory(inventory)
        return inventory
    }

}