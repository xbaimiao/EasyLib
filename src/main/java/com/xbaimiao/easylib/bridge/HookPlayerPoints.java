package com.xbaimiao.easylib.bridge;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * @author 小白
 * @date 2023/5/17 13:37
 **/
public class HookPlayerPoints {

    private static PlayerPointsAPI playerPointsAPI;
    private static boolean setup = false;

    private static void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") == null) {
            throw new NullPointerException("未找到依赖: PlayerPoints");
        }
        playerPointsAPI = ((PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints")).getAPI();
    }

    private static void init() {
        if (!setup) {
            setupPlayerPoints();
            setup = true;
        }
    }

    public static int getPoints(OfflinePlayer player) {
        init();
        if (playerPointsAPI != null) {
            return playerPointsAPI.look(player.getUniqueId());
        } else {
            return -1;
        }
    }

    public static void setPoints(OfflinePlayer player, int amount) {
        init();
        if (playerPointsAPI != null) {
            playerPointsAPI.set(player.getUniqueId(), amount);
        }
    }

    public static boolean hasPoints(OfflinePlayer player, int amount) {
        init();
        return getPoints(player) >= amount;
    }

    public static void addPoints(OfflinePlayer player, int amount) {
        init();
        if (playerPointsAPI != null) {
            playerPointsAPI.give(player.getUniqueId(), amount);
        }
    }

    public static void takePoints(OfflinePlayer player, int amount) {
        init();
        if (playerPointsAPI != null) {
            playerPointsAPI.take(player.getUniqueId(), amount);
        }
    }
}