package com.xbaimiao.easylib.module.ui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class Menu(var title: String, val player: Player) {

    abstract fun build(): Inventory
}

