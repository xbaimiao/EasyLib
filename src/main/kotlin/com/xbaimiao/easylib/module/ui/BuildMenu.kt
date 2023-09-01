package com.xbaimiao.easylib.module.ui

import com.xbaimiao.easylib.module.chat.colored
import com.xbaimiao.easylib.module.item.buildItem
import com.xbaimiao.easylib.module.utils.parseToXMaterial
import com.xbaimiao.easylib.module.utils.warn
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * BuildMenu
 *
 * @author xbaimiao
 * @since 2023/8/23 14:06
 */
class Variable(val key: String, val value: String)

inline fun <reified T : Basic> buildMenu(
    player: Player,
    configuration: ConfigurationSection,
    func: T.() -> Unit
) {
    buildMenu<T>(player, configuration, listOf(), func)
}

inline fun <reified T : Basic> buildMenu(
    player: Player,
    configuration: ConfigurationSection,
    variables: List<Variable>,
    func: T.() -> Unit
) {
    val title = configuration.getString("title", " ")!!.colored()
    val sort = configuration.getStringList("sort").map { it.toCharArray().toList() }

    val items = HashMap<Char, Pair<ItemStack, ConfigurationSection>>()

    val section = configuration.getConfigurationSection("items")
    if (section != null) {
        for (key in section.getKeys(false)) {
            if (key.length > 1) {
                warn("buildMenu: $key is not a char")
                continue
            }
            items[key[0]] = section.convertItem(key, variables) to section.getConfigurationSection(key)!!
        }
    }

    val basic = T::class.java.getConstructor(Player::class.java, String::class.java).newInstance(player, title) as T
    basic.rows(sort.size)
    basic.slots.addAll(sort)

    items.forEach { (k, v) ->
        basic.set(k, v.first)
        basic.setItemSection(k, v.second)
    }

    func(basic)
}

@JvmOverloads
fun ConfigurationSection.convertItem(key: String, variables: List<Variable> = emptyList()): ItemStack {
    var name = this.getString("$key.name", " ")!!.colored()
    for (variable in variables) {
        name = name.replace(variable.key, variable.value)
    }
    val lore = this.getStringList("$key.lore").colored().toMutableList()
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