package com.xbaimiao.easylib.module.utils

import org.bukkit.Bukkit

@Suppress("unused")
object MinecraftVersion {

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
        return version.major <= currentVersion.major
    }

    /**
     * 是否在这个版本之前
     */
    fun isBefore(version: Version): Boolean {
        return version.major > currentVersion.major
    }

    enum class Version(val major: Int) {
        V1_7_10(1710), V1_8_0(1800), V1_8_1(1810), V1_8_2(1820), V1_8_3(1830), V1_8_4(1840), V1_8_5(1850), V1_8_6(
            1860
        ),
        V1_8_7(1870), V1_8_8(1880), V1_8_9(1890), V1_9(1900), V1_9_1(1910), V1_9_2(1920), V1_9_3(1930), V1_9_4(
            1940
        ),
        V1_10(10000), V1_10_1(10010), V1_11(11000), V1_11_1(11010), V1_11_2(11020), V1_12(12000), V1_12_1(
            12010
        ),
        V1_12_2(12020), V1_13(13000), V1_13_1(13010), V1_13_2(13020), V1_14(14000), V1_14_1(14010), V1_14_2(
            14020
        ),
        V1_14_3(14030), V1_14_4(14040), V1_15(15000), V1_15_1(15010), V1_15_2(15020), V1_16(16000), V1_16_1(
            16010
        ),
        V1_16_2(16020), V1_16_3(16030), V1_16_4(16040), V1_16_5(16050), V1_17(17000), V1_17_1(17010), V1_18(
            18000
        ),
        V1_18_1(18010), V1_18_2(18020), V1_19(19000), V1_19_1(19100), V1_19_2(19200), V1_19_3(19300);

        companion object {
            fun formString(version: String): Version {
                return valueOf(String.format("V%s", version.replace(".", "_")))
            }
        }

    }
}