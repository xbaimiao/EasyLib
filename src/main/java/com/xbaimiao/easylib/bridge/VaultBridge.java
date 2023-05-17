package com.xbaimiao.easylib.bridge;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * @author 小白
 * @date 2023/5/17 13:33
 **/
public class VaultBridge {

    private static Economy economy;

    public static Economy getEconomy() {
        return economy;
    }

    private static Permission permission;

    public static Permission getPermission() {
        return permission;
    }

    private static Chat chat;

    public static Chat getChat() {
        return chat;
    }

    static {
        setupEconomy();
        setupPermissions();
        setupChat();
    }

    public static void setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            throw new NullPointerException("未找到依赖: Vault");
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    public static void setupPermissions() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            throw new NullPointerException("未找到依赖: Vault");
        }
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return;
        }
        permission = rsp.getProvider();
    }

    public static void setupChat() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            throw new NullPointerException("未找到依赖: Vault");
        }
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return;
        }
        chat = rsp.getProvider();
    }

}
