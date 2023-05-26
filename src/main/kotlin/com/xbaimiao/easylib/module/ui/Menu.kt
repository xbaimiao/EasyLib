package com.xbaimiao.easylib.module.ui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class Menu(var title: String, val player: Player) {

    abstract fun build(): Inventory

    abstract fun open()

    abstract fun openAsync()

}

fun Player.openMenu(title: String = "chest", func: (Basic) -> Unit) {
    val basic = Basic(this, title)
    func(basic)
    basic.open()
}

