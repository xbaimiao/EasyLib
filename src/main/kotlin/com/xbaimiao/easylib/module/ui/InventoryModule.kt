package com.xbaimiao.easylib.module.ui

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.utils.Module
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

class InventoryModule : Listener, Module<EasyPlugin> {

    override fun enable(plugin: EasyPlugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun disable(plugin: EasyPlugin) {
        HandlerList.unregisterAll(this)
        Bukkit.getOnlinePlayers().forEach {
            if (MenuHolder.fromInventory(it.openInventory.topInventory) != null) {
                it.closeInventory()
            }
        }
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