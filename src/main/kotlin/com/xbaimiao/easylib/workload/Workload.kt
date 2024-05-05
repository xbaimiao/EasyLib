package com.xbaimiao.easylib.workload

import org.bukkit.Bukkit
import org.bukkit.Material
import java.util.*

interface Workload {
    fun compute()
}

/**
 * 填充一个方块
 */
class PlaceBlock(
    private val worldID: UUID,
    private val blockX: Int,
    private val blockY: Int,
    private val blockZ: Int,
    private val material: Material,
) : Workload {

    override fun compute() {
        val world = Bukkit.getWorld(worldID) ?: error("World not found")
        world.getBlockAt(blockX, blockY, blockZ).type = material
    }

}
