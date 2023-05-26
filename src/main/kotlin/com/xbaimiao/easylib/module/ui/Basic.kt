package com.xbaimiao.easylib.module.ui

import com.xbaimiao.easylib.module.utils.submit
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("unused")
class Basic(player: Player, title: String = "chest") : Menu(title, player) {

    /** 行数 **/
    internal var rows = -1

    /** 锁定主手 **/
    internal var handLocked = true

    /** MenuHolder 回调 **/
    internal var holderCallback: ((menu: Basic) -> MenuHolder) = { MenuHolder(it) }

    /** 点击回调 **/
    internal val clickCallback = CopyOnWriteArrayList<(event: InventoryClickEvent) -> Unit>()


    internal val dragCallback = CopyOnWriteArrayList<(event: InventoryDragEvent) -> Unit>()

    /** 关闭回调 **/
    internal var closeCallback: ((event: InventoryCloseEvent) -> Unit) = {}

    /** 只触发一次关闭回调 **/
    internal var onceCloseCallback = false

    /** 物品与对应抽象字符关系 **/
    var items = ConcurrentHashMap<Char, ItemStack>()

    var slotItems = ConcurrentHashMap<Int, ItemStack>()

    /** 抽象字符布局 **/
    var slots = CopyOnWriteArrayList<List<Char>>()

    /**
     * 行数
     * 为 1 - 6 之间的整数，并非原版 9 的倍数
     */
    fun rows(rows: Int) {
        this.rows = rows
    }

    /**
     * 设置是否锁定玩家手部动作
     * 设置为 true 则将阻止玩家在使用菜单时进行包括但不限于
     * 丢弃物品，拿出菜单物品等行为
     *
     * @param handLocked 锁定
     */
    fun handLocked(handLocked: Boolean) {
        this.handLocked = handLocked
    }

    fun onDrag(callback: (event: InventoryDragEvent) -> Unit) {
        dragCallback += callback
    }

    /**
     * 页面关闭时触发回调
     * 只能触发一次（玩家客户端强制关闭时会触发两次原版 InventoryCloseEvent 事件）
     */
    fun onClose(once: Boolean = true, callback: (event: InventoryCloseEvent) -> Unit) {
        closeCallback = callback
        onceCloseCallback = once
    }

    /**
     * 点击事件回调
     * 仅在特定位置下触发
     */
    fun onClick(bind: Int, callback: (event: InventoryClickEvent) -> Unit) {
        onClick {
            if (it.rawSlot == bind) {
                it.isCancelled = true
                callback(it)
            }
        }
    }

    fun onClick(bind: Char, callback: (event: InventoryClickEvent) -> Unit) {
        onClick {
            if (this.getSlot(it.rawSlot) == bind) {
                it.isCancelled = true
                callback(it)
            }
        }
    }

    /**
     * 整页点击事件回调
     * 可选是否自动锁定点击位置
     */
    fun onClick(callback: (event: InventoryClickEvent) -> Unit) {
        clickCallback += callback
    }

    /**
     * 使用抽象字符页面布局
     */
    fun map(vararg slots: String) {
        this.slots.clear()
        this.slots.addAll(slots.map { it.toCharArray().toList() })
        // 自动修改行数
        if (rows < slots.size) {
            rows = slots.size
        }
    }

    fun map(slots: List<String>) {
        this.slots.clear()
        this.slots.addAll(slots.map { it.toCharArray().toList() })
        // 自动修改行数
        if (rows < slots.size) {
            rows = slots.size
        }
    }

    /**
     * 根据抽象符号设置物品
     */
    fun set(slot: Char, itemStack: ItemStack) {
        items[slot] = itemStack
    }

    fun set(slot: Int, itemStack: ItemStack) {
        slotItems[slot] = itemStack
    }

    /**
     * 获取位置对应的抽象字符
     */
    fun getSlot(slot: Int): Char {
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                if (row * 9 + cel == slot) {
                    return line[cel]
                }
                cel++
            }
            row++
        }
        return ' '
    }

    /**
     * 获取抽象字符对应的位置
     */
    fun getSlots(slot: Char): List<Int> {
        val list = mutableListOf<Int>()
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                if (line[cel] == slot) {
                    list.add(row * 9 + cel)
                }
                cel++
            }
            row++
        }
        return list
    }

    private fun createTitle(): String {
        return title
    }

    /**
     * 构建页面
     */
    override fun build(): Inventory {
        val inventory =
            Bukkit.createInventory(holderCallback(this), if (rows > 0) rows * 9 else slots.size * 9, createTitle())
        var row = 0
        while (row < slots.size) {
            val line = slots[row]
            var cel = 0
            while (cel < line.size && cel < 9) {
                inventory.setItem(row * 9 + cel, items[line[cel]] ?: ItemStack(Material.AIR))
                cel++
            }
            row++
        }
        slotItems.forEach { (k, v) ->
            inventory.setItem(k, v)
        }
        return inventory
    }

    override fun open() {
        player.openInventory(build())
    }

    override fun openAsync() {
        submit(async = true) {
            val inventory = build()
            submit {
                player.openInventory(inventory)
            }
        }
    }

    companion object {
        fun asyncBuildAndOpen(player: Player, title: String, build: Basic.() -> Unit) {
            submit(async = true) {
                val basic = Basic(player, title)
                build.invoke(basic)
                basic.openAsync()
            }
        }

    }

}