package com.xbaimiao.easylib.module.ui

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

abstract class Menu(var title: String, val player: Player) {

    private val itemSectionMap = HashMap<Char, ConfigurationSection>()

    fun getItemSection(char: Char): ConfigurationSection? {
        return itemSectionMap[char]
    }

    fun setItemSection(char: Char, section: ConfigurationSection) {
        itemSectionMap[char] = section
    }

    abstract fun build(): Inventory

    abstract fun open()

    abstract fun openAsync()

}