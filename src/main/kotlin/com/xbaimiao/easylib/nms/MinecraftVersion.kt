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

@RuntimeResources(
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e3c5450d-combined.csrg",
        hash = "ec52bfc2822dd8385c619f6e80e106baab1c1454",
        zip = true,
        tag = "1.17:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e3c5450d-fields.csrg",
        hash = "44caa1f63bd20d807bd92d13d2fe291b482c0771",
        zip = true,
        tag = "1.17:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-00fabbe5-combined.csrg",
        hash = "a1a36e589321cd782aa9f0917bc0a1516a69de3d",
        zip = true,
        tag = "1.17.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-00fabbe5-fields.csrg",
        hash = "6e515ad1b4cd49e93e26380e4deca8b876a517a7",
        zip = true,
        tag = "1.17.1:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18-cl.csrg",
        hash = "9a3742d6b84542d263c7309fb5a23066a113e307",
        zip = true,
        tag = "1.18:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-8e9479b6-members.csrg",
        hash = "805efea073022d30cab12cd511513751af80789c",
        zip = true,
        tag = "1.18:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18.1-cl.csrg",
        hash = "9a3742d6b84542d263c7309fb5a23066a113e307",
        zip = true,
        tag = "1.18.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-e0c6d16a-members.csrg",
        hash = "6d0d8df7538d9e0006ff2f9c01a4125d699e857b",
        zip = true,
        tag = "1.18.1:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.18.2-cl.csrg",
        hash = "bcf6240fb6a77d326538f61a822334f9ff65c9ec",
        zip = true,
        tag = "1.18.2:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-732abad1-members.csrg",
        hash = "e51e094f2888a44d12d0f3d42305afc2675c6748",
        zip = true,
        tag = "1.18.2:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19-cl.csrg",
        hash = "44eaa87a517f3fb7661afe387edd68669b782435",
        zip = true,
        tag = "1.19:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-2f7944eb-members.csrg",
        hash = "213f64b57f20f414309125b1f4eb7cbbcf159508",
        zip = true,
        tag = "1.19:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.1-cl.csrg",
        hash = "3cee4d607a86f0a7e1dd2a6fb669a2644e4d400c",
        zip = true,
        tag = "1.19.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-550f788f-members.csrg",
        hash = "709b9250af770537cc8b23f734ac31dbeee6dc6e",
        zip = true,
        tag = "1.19.1:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.2-cl.csrg",
        hash = "c77adbc7fdc2df0b274e2eafecbf3f820ebd710e",
        zip = true,
        tag = "1.19.2:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-8b4db36a-members.csrg",
        hash = "069e5d3e081c303231ea453ed1e56ac149917c9e",
        zip = true,
        tag = "1.19.2:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.3-cl.csrg",
        hash = "496893661f336066292d50ea5ce12acba20cb2c5",
        zip = true,
        tag = "1.19.3:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-c69e3af0-members.csrg",
        hash = "65539f95551a967e096f916896c031c5969d2e34",
        zip = true,
        tag = "1.19.3:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.19.4-cl.csrg",
        hash = "523cd77ba9aa25f0f59cebee1eb6b3e9a4e9c602",
        zip = true,
        tag = "1.19.4:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-0207e96c-members.csrg",
        hash = "a357798e3ce7e23f857e833914b765fca3b8ca4c",
        zip = true,
        tag = "1.19.4:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.20-cl.csrg",
        hash = "1e2870b303f37a07709c2045b5db7e6c79e48acd",
        zip = true,
        tag = "1.20:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-c571a01f-members.csrg",
        hash = "de0d266adbbff4f7ffe4dd44ed0e36f9205b31b1",
        zip = true,
        tag = "1.20:fields"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-1.20.1-cl.csrg",
        hash = "dc9d85b9aff4158fe86b105e93998a1bde83996f",
        zip = true,
        tag = "1.20.1:combined"
    ),
    RuntimeResource(
        value = "https://skymc.oss-cn-shanghai.aliyuncs.com/taboolib/resources/bukkit-8788cf22-members.csrg",
        hash = "96e3ddb7a5d6378a0f7dede3f678d15994db8fc9",
        zip = true,
        tag = "1.20.1:fields"
    )
)
class MappingFile(val combined: String, val fields: String) {

    companion object {

        val files = MappingFile::class.java.getDeclaredAnnotation(RuntimeResources::class.java).value
            .groupBy { it.tag.split(':')[0] }
            .map {
                it.key to MappingFile(
                    it.value.first { a -> a.tag.split(':')[1] == "combined" }.hash,
                    it.value.first { a -> a.tag.split(':')[1] == "fields" }.hash
                )
            }.toMap()
    }
}

@Suppress("unused")
object MinecraftVersion {

    val minecraftVersion by unsafeLazy {
        Bukkit.getServer().javaClass.name.split('.')[3]
    }

    /**
     * 当前运行的版本（数字版本），例如：1.8.8
     */
    val runningVersion by unsafeLazy {
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
        return version.major <= currentVersion.major
    }

    /**
     * 是否在这个版本之前
     */
    fun isBefore(version: Version): Boolean {
        return version.major > currentVersion.major
    }

    /**
     * 当前版本是否高于等于你输入的版本
     */
    fun isHigherOrEqual(version: Version): Boolean {
        return version.major <= currentVersion.major
    }

    /**
     * 是否为 1.17 以上版本
     */
    val isUniversal by lazy {
        isHigherOrEqual(Version.V1_17)
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
        V1_18_1(18010), V1_18_2(18020), V1_19(19000), V1_19_1(19100), V1_19_2(19200), V1_19_3(19300), V1_19_4(
            19400
        ),
        V1_20_0(20000), V1_20_1(20001), UNKNOWN(0);

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