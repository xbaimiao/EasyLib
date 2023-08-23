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
inline fun <reified T : Basic> buildMenu(player: Player, configuration: ConfigurationSection, func: T.() -> Unit) {
    val title = configuration.getString("title", " ")!!.colored()
    val sort = configuration.getStringList("sort").map { it.toCharArray().toList() }

    val items = HashMap<Char, ItemStack>()

    val section = configuration.getConfigurationSection("items")
    if (section != null) {
        for (key in section.getKeys(false)) {
            if (key.length > 1) {
                warn("buildMenu: $key is not a char")
            }
            val name = section.getString("$key.name", " ")!!.colored()
            val material = section.getString("$key.material", "STONE")!!.parseToXMaterial()
            val lore = section.getStringList("$key.lore").colored()
            val customModelData = section.getInt("$key.custom_model_data")

            items[key[0]] = buildItem(material) {
                this.name = name
                this.lore.addAll(lore)
                this.customModelData = customModelData
            }
        }
    }

    val basic = T::class.java.getConstructor(Player::class.java, String::class.java).newInstance(player, title) as T
    basic.rows(sort.size)
    basic.slots.addAll(sort)

    items.forEach { (k, v) ->
        basic.set(k, v)
    }

    func(basic)
}