package com.xbaimiao.easylib.util

/**
 * @author 小白
 * @date 2023/4/18 09:52
 */
object ServerChecker {

    /**
     * 检查这个服务器是否运行的Folia
     */
    val isFolia by lazy {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

    val isPaper by lazy {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            true
        } catch (ignored: ClassNotFoundException) {
            false
        }
    }

}