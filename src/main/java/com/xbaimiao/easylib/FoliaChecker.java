package com.xbaimiao.easylib;

/**
 * @author 小白
 * @date 2023/4/18 09:52
 **/
public final class FoliaChecker {
    private static final boolean IS_FOLIA;

    static {
        boolean isFolia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException ignored) {
            isFolia = false;
        }
        IS_FOLIA = isFolia;
    }

    /**
     * Check if the server is running Folia
     *
     * @return true if the server is running Folia
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

}