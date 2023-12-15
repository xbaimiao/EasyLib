package com.xbaimiao.easylib.nms

import com.xbaimiao.easylib.util.plugin
import com.xbaimiao.easylib.util.submit
import com.xbaimiao.easylib.util.unsafeLazy
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

/**
 * 获取 OBC 类
 */
fun obcClass(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${MinecraftVersion.minecraftVersion}.$name")
}

/**
 * 获取 NMS 类
 */
fun nmsClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversal) {
        Class.forName(
            MinecraftVersion.mapping.classMap[name]?.replace('/', '.') ?: error("Cannot find nms class: $name")
        )
    } else {
        Class.forName("net.minecraft.server.${MinecraftVersion.minecraftVersion}.$name")
    }
}

/**
 * 向玩家发送数据包（异步）
 */
fun Player.sendPacket(packet: Any): CompletableFuture<Void> {
    val future = CompletableFuture<Void>()
    submit(async = true) {
        try {
            sendPacketBlocking(packet)
            future.complete(null)
        } catch (e: Throwable) {
            future.completeExceptionally(e)
            e.printStackTrace()
        }
    }
    return future
}

/**
 * 向玩家发送数据包
 */
fun Player.sendPacketBlocking(packet: Any) {
    PacketSender.sendPacket(this, packet)
}

@Suppress("unused")
object MinecraftVersion {

    val minecraftVersion by unsafeLazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    /**
     * 当前运行的版本（数字版本），例如：1.8.8
     */
    private val runningVersion by unsafeLazy {
        val version = Bukkit.getServer().version.split("MC:")[1]
        version.substring(0, version.length - 1).trim()
    }

    val mapping by unsafeLazy {
        val mappingFile = if (isUniversal) {
            MappingFile.files[runningVersion]
        } else {
            MappingFile.files["1.17"]!!
        }
        if (mappingFile == null) {
            Bukkit.getPluginManager().disablePlugin(plugin)
            error("UnsupportedVersionException")
        }
        Mapping(
            FileInputStream("assets/${mappingFile.combined.substring(0, 2)}/${mappingFile.combined}"),
            FileInputStream("assets/${mappingFile.fields.substring(0, 2)}/${mappingFile.fields}"),
        )
    }

    /**
     * 服务器运行的版本
     */
    @JvmStatic
    val currentVersion: Version by lazy {
        val v = Bukkit.getBukkitVersion().split("-".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[0].replace(" ", "")
        Version.formString(v)
    }

    /**
     * 是否在这个版本之后 (包括这个版本)
     */
    fun isAfter(version: Version): Boolean {
        return version.majorLegacy <= currentVersion.majorLegacy
    }

    /**
     * 是否在这个版本之前
     */
    fun isBefore(version: Version): Boolean {
        return version.majorLegacy > currentVersion.majorLegacy
    }

    /**
     * 当前版本是否高于等于你输入的版本
     */
    fun isHigherOrEqual(version: Version): Boolean {
        return version.majorLegacy <= currentVersion.majorLegacy
    }

    /**
     * 是否为 1.17 以上版本
     */
    val isUniversal by lazy {
        isHigherOrEqual(Version.V1_17)
    }

    enum class Version(val majorLegacy: Int, val major: Int) {
        V1_7_10(1710, -1), // -1
        V1_8_0(1800, 0), V1_8_3(1830, 0), V1_8_4(1840, 0), V1_8_5(1850, 0),
        V1_8_6(1860, 0), V1_8_7(1870, 0), V1_8_8(1880, 0), V1_8_9(1890, 0), // 0
        V1_9(1900, 1), V1_9_2(1920, 1), V1_9_4(1940, 1), // 1
        V1_10(10000, 2), V1_10_1(10010, 2), V1_10_2(10020, 2),  // 2
        V1_11(11000, 3), V1_11_1(11010, 3), V1_11_2(11020, 3), // 3
        V1_12(12000, 4), V1_12_1(12010, 4), V1_12_2(12020, 4),  // 4
        V1_13(13000, 5), V1_13_1(13010, 5), V1_13_2(13020, 5), //5
        V1_14(14000, 6), V1_14_1(14010, 6), V1_14_2(14020, 6), V1_14_3(14030, 6), V1_14_4(14040, 6), // 6
        V1_15(15000, 7), V1_15_1(15010, 7), V1_15_2(15020, 7),  //7
        V1_16(16000, 8), V1_16_1(16010, 8), V1_16_2(16020, 8),
        V1_16_3(16030, 8), V1_16_4(16040, 8), V1_16_5(16050, 8),//8
        V1_17(17000, 9), V1_17_1(17010, 9),
        V1_18(18000, 10), V1_18_1(18010, 10), V1_18_2(18020, 10), //10
        V1_19(19000, 11), V1_19_1(19100, 11), V1_19_2(19200, 11), V1_19_3(19300, 11), V1_19_4(19400, 11), //11
        V1_20_0(20000, 12), V1_20_1(20001, 12), V1_20_2(20002, 12), V1_20_3(20003, 12), //12
        UNKNOWN(0, -999);

        companion object {
            fun formString(version: String): Version {
                return runCatching { valueOf(String.format("V%s", version.replace(".", "_"))) }.getOrElse { UNKNOWN }
            }
        }

        override fun toString(): String {
            return this.name.replace("V", "").replace("_", ".")
        }

    }
}