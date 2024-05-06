package com.xbaimiao.easylib.workload

import org.bukkit.Location
import org.bukkit.World

data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val minZ: Double,
    val maxX: Double,
    val maxY: Double,
    val maxZ: Double,
    val world: World,
) {

    val min by lazy { Location(world, minX, minY, minZ) }
    val max by lazy { Location(world, maxX, maxY, maxZ) }

    companion object {

        fun of(location1: Location, location2: Location): BoundingBox {
            val world = location1.world ?: error("World not found")
            val x1 = location1.blockX.toDouble()
            val y1 = location1.blockY.toDouble()
            val z1 = location1.blockZ.toDouble()
            val x2 = location2.blockX.toDouble()
            val y2 = location2.blockY.toDouble()
            val z2 = location2.blockZ.toDouble()

            return BoundingBox(
                minX = x1.coerceAtMost(x2),
                minY = y1.coerceAtMost(y2),
                minZ = z1.coerceAtMost(z2),
                maxX = x1.coerceAtLeast(x2),
                maxY = y1.coerceAtLeast(y2),
                maxZ = z1.coerceAtLeast(z2),
                world
            )
        }

    }

}
