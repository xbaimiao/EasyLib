package com.xbaimiao.easylib.bridge;

import org.bukkit.OfflinePlayer;

/**
 * @author 小白
 * @date 2023/5/17 13:36
 **/
public class HookVault {

    public static void takeMoney(OfflinePlayer player, double money) {
        if (VaultBridge.getEconomy() != null) VaultBridge.getEconomy().withdrawPlayer(player, money);
    }

    public static void addMoney(OfflinePlayer player, double money) {
        if (VaultBridge.getEconomy() != null) VaultBridge.getEconomy().depositPlayer(player, money);
    }

    public static boolean hasMoney(OfflinePlayer player, double money) {
        if (VaultBridge.getEconomy() != null) {
            return VaultBridge.getEconomy().has(player, money);
        } else {
            return false;
        }
    }

    public static double getMoney(OfflinePlayer player) {
        if (VaultBridge.getEconomy() != null) {
            return VaultBridge.getEconomy().getBalance(player);
        } else {
            return 0.0;
        }
    }

    public void setMoney(OfflinePlayer player, double money) {
        addMoney(player, money - getMoney(player));
    }

}