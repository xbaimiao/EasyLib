package com.xbaimiao.easylib.ui

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.task.EasyLibTask
import com.xbaimiao.easylib.util.submit
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

object UIHandler : Listener {

    private var task: EasyLibTask? = null

    fun enable(plugin: EasyPlugin) {
        startUpdateTask()
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun disable() {
        stopUpdateTask()
        HandlerList.unregisterAll(this)
        Bukkit.getOnlinePlayers().forEach { it.closeInventory() }
    }

    private fun startUpdateTask() {
        task = submit(period = 20, async = true) {
            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                val inventory = onlinePlayer.openInventory.topInventory
                val menu = MenuHolder.fromInventory(inventory) ?: continue
                for ((slot, update) in menu.slotUpdate) {
                    if (!update.canUpdate()) {
                        continue
                    }
                    if (!update.async) {
                        submit {
                            inventory.setItem(slot, update.update())
                        }
                        continue
                    }
                    inventory.setItem(slot, update.update())
                }
                for ((char, update) in menu.itemsUpdate) {
                    if (!update.canUpdate()) {
                        continue
                    }
                    if (!update.async) {
                        submit {
                            for (slot in menu.getSlots(char)) {
                                inventory.setItem(slot, update.update())
                            }
                        }
                        continue
                    }
                    for (slot in menu.getSlots(char)) {
                        inventory.setItem(slot, update.update())
                    }
                }
            }
        }
    }

    private fun stopUpdateTask() {
        task?.cancel()
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val builder = MenuHolder.fromInventory(event.inventory) ?: return
        if (builder.player.name != event.whoClicked.name) {
            return
        }
        // 锁定主手
        if (builder.handLocked && (event.rawSlot - event.inventory.size - 27 == event.whoClicked.inventory.heldItemSlot || event.click == org.bukkit.event.inventory.ClickType.NUMBER_KEY && event.hotbarButton == event.whoClicked.inventory.heldItemSlot)) {
            event.isCancelled = true
        }
        // 处理事件
        try {
            builder.clickCallback.forEach { it(event) }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }


    @EventHandler
    fun onDrag(e: InventoryDragEvent) {
        val builder = MenuHolder.fromInventory(e.inventory) ?: return
        if (builder.player.name != e.whoClicked.name) {
            return
        }
        builder.dragCallback.forEach { it.invoke(e) }
    }

    @EventHandler
    fun close(event: InventoryCloseEvent) {
        val close = MenuHolder.fromInventory(event.inventory) ?: return
        if (close.player.name != event.player.name) {
            return
        }
        close.closeCallback.invoke(event)
        // 只触发一次
        if (close.onceCloseCallback) {
            close.closeCallback = {}
        }
    }

}
