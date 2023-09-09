package com.xbaimiao.easylib.ui

import com.xbaimiao.easylib.chat.colored
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class Slot(
    val slot: Int,
    val basic: Basic
) {

    var material = Material.AIR
    var displayName = ""
    var lore = listOf<String>()
    var customModelData = 0

    internal var onClick: ((event: InventoryClickEvent) -> Unit)? = null
    internal var onDrag: ((event: InventoryDragEvent) -> Unit)? = null

    fun onClick(onClick: (event: InventoryClickEvent) -> Unit) {
        this.onClick = onClick
        basic.onClick(slot) {
            onClick(it)
        }
    }

    fun onDrag(onDrag: (event: InventoryDragEvent) -> Unit) {
        this.onDrag = onDrag
        basic.onDrag {
            onDrag(it)
        }
    }

    fun buildItem(): ItemStack {
        return com.xbaimiao.easylib.util.buildItem(material) {
            name = displayName.colored()
            lore.addAll(this@Slot.lore)
            customModelData = this@Slot.customModelData
        }
    }

}