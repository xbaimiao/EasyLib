package com.xbaimiao.easylib.ui

import com.xbaimiao.easylib.bridge.replacePlaceholder
import com.xbaimiao.easylib.chat.colored
import com.xbaimiao.easylib.util.buildItem
import com.xbaimiao.easylib.util.parseToXMaterial
import com.xbaimiao.easylib.util.warn
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

abstract class Menu(val player: Player) {

    private val itemSectionMap = HashMap<Char, ConfigurationSection>()

    abstract fun build(): Inventory

    abstract fun open()

    abstract fun openAsync()

}

@Suppress("LeakingThis")
open class MenuHolder(val menu: Basic) : InventoryHolder {

    private val inventory =
        Bukkit.createInventory(this, if (menu.rows > 0) menu.rows * 9 else menu.slots.size * 9, "chest")

    override fun getInventory(): Inventory {
        return inventory
    }

    companion object {

        fun fromInventory(inventory: Inventory): Basic? {
            return (inventory.holder as? MenuHolder)?.menu
        }
    }
}

class MenuUpdate(
    val async: Boolean,
    val period: Long,
    val getItem: () -> ItemStack,
) {

    private var lastupdate = 0L

    fun canUpdate(): Boolean {
        return System.currentTimeMillis() > (lastupdate + (period * 50))
    }

    fun update(): ItemStack {
        lastupdate = System.currentTimeMillis()
        return getItem.invoke()
    }

}

class Variable(val key: String, val value: String)

fun Basic.buildMenu(
    configuration: ConfigurationSection,
    onSetItem: Basic.(Char, ItemStack, ConfigurationSection) -> Unit = { _, _, _ -> },
    func: Basic.() -> Unit = {},
): Basic {
    return buildMenu(configuration, listOf(), onSetItem, func)
}

fun Basic.buildMenu(
    configuration: ConfigurationSection,
    variables: List<Variable>,
    onSetItem: Basic.(Char, ItemStack, ConfigurationSection) -> Unit = { _, _, _ -> },
    func: Basic.() -> Unit = {},
): Basic {
    val basic = this
    var list = configuration.getStringList("layout")
    if (list.isEmpty()) {
        list = configuration.getStringList("sort")
    }
    val sort = list.map { it.toCharArray().toList() }

    val items = HashMap<Char, Pair<ItemStack, ConfigurationSection>>()

    val section = configuration.getConfigurationSection("items")
    if (section != null) {
        for (key in section.getKeys(false)) {
            if (key.length > 1) {
                warn("buildMenu: $key is not a char")
                continue
            }
            items[key[0]] = section.convertItem(basic.player, key, variables) to section.getConfigurationSection(key)!!
        }
    }

    basic.rows(sort.size)
    basic.slots.addAll(sort)

    items.forEach { (k, v) ->
        basic.set(k, v.first)
        onSetItem(basic, k, v.first, v.second)
    }

    func(basic)
    return basic
}

@JvmOverloads
fun ConfigurationSection.convertItem(player: Player, key: String, variables: List<Variable> = emptyList()): ItemStack {
    var name = this.getString("$key.name", " ")!!.colored().replacePlaceholder(player)
    for (variable in variables) {
        name = name.replace(variable.key, variable.value)
    }
    val lore = this.getStringList("$key.lore").colored().replacePlaceholder(player).toMutableList()
    lore.replaceAll {
        var result = it
        for (variable in variables) {
            result = result.replace(variable.key, variable.value)
        }
        result
    }
    var customModelData = this.getString("$key.custom_model_data", "0")!!
    for (variable in variables) {
        customModelData = customModelData.replace(variable.key, variable.value)
    }

    val xMaterial = this.getString("$key.material", "STONE")!!.parseToXMaterial()

    return buildItem(xMaterial) {
        this.name = name
        this.damage = this@convertItem.getInt("$key.durability")
        this.lore.addAll(lore)
        customModelData.toIntOrNull()?.let {
            this.customModelData = it
        }
    }
}
