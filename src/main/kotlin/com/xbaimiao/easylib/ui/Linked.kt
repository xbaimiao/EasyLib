package com.xbaimiao.easylib.ui

import com.xbaimiao.easylib.util.isNotAir
import com.xbaimiao.easylib.util.subList
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @author xbaimiao
 * @date 2024/8/7
 * @email owner@xbaimiao.com
 * 多页菜单
 */
abstract class Linked<T>(player: Player) : Basic(player) {

    var page: Int = 0
    private var elements: Collection<T> = listOf()
    private var elementSlot: List<Int> = Slots.CENTER
    private var onGenerateCallback: (element: T, index: Int, slot: Int) -> ItemStack = { _, _, _ ->
        ItemStack(Material.AIR)
    }
    private val elementMap = HashMap<Int, T>()

    /**
     * 是否可以返回上一页
     */
    fun hasPreviousPage(): Boolean {
        return page > 0
    }

    /**
     * 是否可以前往下一页
     */
    fun hasNextPage(): Boolean {
        return elements.size / elementSlot.size.toDouble() > (page + 1)
    }

    /**
     * 设置下一页按钮
     */
    fun setNextPage(vararg slot: Int, callback: (page: Int, hasNextPage: Boolean) -> ItemStack) {
        slot.forEach {
            // 设置物品
            set(it, callback(page, hasNextPage()))
            // 点击事件
            onClick(it) {
                if (hasNextPage()) {
                    page++
                    // 刷新页面
                    player.openInventory(build())
                }
            }
        }
    }

    /**
     * 设置上一页按钮
     */
    fun setPreviousPage(vararg slot: Int, callback: (page: Int, hasPreviousPage: Boolean) -> ItemStack) {
        slot.forEach {
            // 设置物品
            set(it, callback(page, hasPreviousPage()))
            // 点击事件
            onClick(it) {
                if (hasPreviousPage()) {
                    page--
                    // 刷新页面
                    player.openInventory(build())
                }
            }
        }
    }

    fun elements(elements: Collection<T>) {
        this.elements = elements
    }

    fun slots(elementSlot: Collection<Int>) {
        this.elementSlot = elementSlot.toList()
    }

    fun onGenerate(callback: (element: T, index: Int, slot: Int) -> ItemStack) {
        onGenerateCallback = callback
    }

    fun onClick(callback: (element: T, event: InventoryClickEvent) -> Unit) {
        super.onClick click@{
            val element = elementMap[it.rawSlot] ?: return@click
            callback(element as T, it)
        }
    }

    override fun handleInventory(inventory: Inventory) {
        super.handleInventory(inventory)
        elementMap.clear()
        val elementItems = subList(elements, page * elementSlot.size, (page + 1) * elementSlot.size)
        elementItems.forEachIndexed { index, t ->
            val slot = elementSlot.getOrNull(index) ?: return@forEachIndexed
            elementMap[slot] = t
            val itemStack = onGenerateCallback(t, index, slot)
            if (itemStack.isNotAir()) {
                inventory.setItem(slot, itemStack)
            }
        }
    }

}
