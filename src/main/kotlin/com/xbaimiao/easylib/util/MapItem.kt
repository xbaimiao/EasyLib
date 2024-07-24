package com.xbaimiao.easylib.util

import com.cryptomorin.xseries.XMaterial
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapPalette
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

/**
 * 创建地图画（堵塞）
 *
 * @param url 图像地址
 * @param width 图像宽度
 * @param height 图像高度
 */
fun buildMap(
    url: URL,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {},
): MapItem {
    return MapItem(url.openStream().use { ImageIO.read(it) }.zoomed(width, height), builder)
}

/**
 * 创建地图画（堵塞）
 *
 * @param file 图像文件
 * @param width 图像宽度
 * @param height 图像高度
 */
fun buildMap(
    file: File,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {},
): MapItem {
    return MapItem(ImageIO.read(file).zoomed(width, height), builder)
}

/**
 * 创建地图画（堵塞）
 *
 * @param image 图像对象
 * @param width 图像宽度
 * @param height 图像高度
 */
fun buildMap(
    image: BufferedImage,
    width: Int = 128,
    height: Int = 128,
    builder: ItemBuilder.() -> Unit = {},
): MapItem {
    return MapItem(image.zoomed(width, height), builder)
}

/**
 * 调整图片分辨率
 * 地图最佳显示分辨率为128*128
 */
fun BufferedImage.zoomed(width: Int = 128, height: Int = 128): BufferedImage {
    val tag = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    tag.graphics.drawImage(this, 0, 0, width, height, null)
    return tag
}

class MapItem(val image: BufferedImage, val builder: ItemBuilder.() -> Unit = {}) {


    val mapRenderer = object : MapRenderer() {

        var rendered = false

        override fun render(mapView: MapView, mapCanvas: MapCanvas, player: Player) {
            if (rendered) {
                return
            }
            mapCanvas.drawImage(0, 0, MapPalette.resizeImage(image))
            mapView.isTrackingPosition = false
            rendered = true
        }
    }

    val mapView by lazy {
        val mapView = Bukkit.createMap(Bukkit.getWorlds()[0])
        mapView.renderers.clear()
        mapView.addRenderer(mapRenderer)
        mapView.isTrackingPosition = false
        mapView.isLocked = false
        mapView
    }

    val mapItem by lazy {
        val map = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.Version.V1_13)) {
            buildItem(XMaterial.FILLED_MAP, builder)
        } else {
            buildItem(XMaterial.FILLED_MAP) {
                damage = mapView.invokeMethod<Short>("getId")!!.toInt()
                builder(this)
            }
        }
        if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.Version.V1_13)) {
            map.modifyMeta<MapMeta> { mapView = this@MapItem.mapView }
        } else {
            map
        }
    }

}
