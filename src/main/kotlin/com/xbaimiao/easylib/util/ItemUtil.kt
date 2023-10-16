package com.xbaimiao.easylib.util

import com.xbaimiao.easylib.chat.colored
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta


/**
 * 判断是不是空气
 */
fun ItemStack?.isAir(): Boolean {
    return this == null || this.type == Material.AIR || this.type.name.endsWith("_AIR")
}

val ItemStack.displayName
    get() = kotlin.runCatching {
        if (itemMeta.hasDisplayName()) {
            itemMeta.displayName
        } else {
            type.name
        }
    }.getOrDefault(type.name)

/**
 * 判断是否不是空气
 */
fun ItemStack?.isNotAir(): Boolean {
    return !this.isAir()
}

fun ItemStack.hasName(name: String): Boolean {
    if (this.isAir()) {
        return false
    }
    val itemMeta = itemMeta ?: return false
    if (!itemMeta.hasDisplayName()) {
        return false
    }
    return itemMeta.displayName.contains(name.colored())
}

fun ItemStack.hasLore(lore: String): Boolean {
    if (!this.hasLore()) {
        return false
    }
    return this.itemMeta!!.lore.toString().contains(lore)
}

fun Player.giveItem(itemStack: ItemStack, drop: Boolean = true) {
    this.inventory.addItem(itemStack).values.forEach {
        if (drop) {
            this.world.dropItem(this.location, it)
        }
    }
}

/**
 * 判断是否有lore
 */
fun ItemStack.hasLore(): Boolean {
    if (isAir()) {
        return false
    }
    val itemMeta = this.itemMeta ?: return false
    return itemMeta.hasLore() && itemMeta.lore != null
}

fun Inventory.hasItem(amount: Int = 1, matcher: ItemStack.() -> Boolean): Boolean {
    var checkAmount = amount
    for (itemStack in this.contents) {
        if (itemStack.isNotAir() && matcher.invoke(itemStack!!)) {
            checkAmount -= itemStack.amount
            if (checkAmount <= 0) {
                return true
            }
        }
    }
    return false
}

fun Inventory.takeItem(amount: Int = 1, matcher: ItemStack.() -> Boolean): Boolean {
    var takeAmount = amount
    for (i in this.contents.indices) {
        val itemStack = this.contents.getOrNull(i) ?: continue
        if (itemStack.isNotAir() && matcher.invoke(itemStack)) {
            takeAmount -= itemStack.amount
            if (takeAmount < 0) {
                itemStack.amount = itemStack.amount - (takeAmount + itemStack.amount)
                return true
            } else {
                this.setItem(i, null)
                if (takeAmount == 0) {
                    return true
                }
            }
        }
    }
    return false
}

fun Inventory.hasItem(itemStack: ItemStack, amount: Int): Boolean {
    return this.hasItem(amount) { this.isSimilar(itemStack) }
}

fun <T : ItemMeta> ItemStack.modifyMeta(apply: T.() -> Unit): ItemStack {
    if (isAir()) {
        error("air")
    }
    return also { itemMeta = ((itemMeta as? T)?.also(apply) ?: itemMeta) }
}