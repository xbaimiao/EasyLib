package com.xbaimiao.easylib.workload

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.util.BoundingBox

/**
 * 分批次填充两点之间的方块
 */
class FillBox(private val workloadRunnable: WorkloadRunnable) {

    fun fill(cornerA: Location, cornerB: Location, material: Material) {
        if (cornerA.world != cornerB.world) {
            error("cornerA and cornerB must be in the same world")
        }

        val box: BoundingBox = BoundingBox.of(cornerA.block, cornerB.block)
        val max = box.max
        val min = box.min

        val world = cornerA.world ?: error("World not found")

        for (x in min.blockX..max.blockX) {
            for (y in min.blockY..max.blockY) {
                for (z in min.blockZ..max.blockZ) {
                    val placeBlock = PlaceBlock(world.uid, x, y, z, material)
                    this.workloadRunnable.addWorkload(placeBlock)
                }
            }
        }
    }

}
