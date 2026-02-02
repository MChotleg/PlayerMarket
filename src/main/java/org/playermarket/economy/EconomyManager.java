package org.playermarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;

public class EconomyManager {
    private final PlayerMarket plugin;
    private Economy economy;
    private boolean enabled = false;
    private String providerName = "未知";

    public EconomyManager(PlayerMarket plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                plugin.getLogger().warning(I18n.get("economy.vault.notfound"));
                return false;
            }

            RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                    .getServicesManager().getRegistration(Economy.class);

            if (rsp == null) {
                plugin.getLogger().warning(I18n.get("economy.provider.notfound"));
                return false;
            }

            economy = rsp.getProvider();
            providerName = economy.getName();
            enabled = true;
            plugin.getLogger().info(I18n.get("economy.connected", providerName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(I18n.get("economy.init.failed", e.getMessage()));
            return false;
        }
    }

    // 检查经济系统是否可用
    public boolean isEconomyEnabled() {
        return enabled && economy != null;
    }

    // 获取经济提供者名称
    public String getProviderName() {
        return providerName;
    }

    public double getBalance(Player player) {
        if (!isEconomyEnabled()) return 0.0;
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.getbalance.failed", e.getMessage()));
            return 0.0;
        }
    }
    
    public double getBalance(java.util.UUID playerUuid) {
        if (!isEconomyEnabled()) return 0.0;
        try {
            return economy.getBalance(Bukkit.getOfflinePlayer(playerUuid));
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.getbalance.failed", e.getMessage()));
            return 0.0;
        }
    }

    public boolean deposit(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            economy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.deposit.failed", e.getMessage()));
            return false;
        }
    }
    
    public boolean deposit(java.util.UUID playerUuid, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            economy.depositPlayer(Bukkit.getOfflinePlayer(playerUuid), amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.deposit.failed", e.getMessage()));
            return false;
        }
    }

    public boolean withdraw(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.withdraw.failed", e.getMessage()));
            return false;
        }
    }
    
    public boolean withdraw(java.util.UUID playerUuid, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            if (economy.has(Bukkit.getOfflinePlayer(playerUuid), amount)) {
                economy.withdrawPlayer(Bukkit.getOfflinePlayer(playerUuid), amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.withdraw.failed", e.getMessage()));
            return false;
        }
    }

    public String format(double amount) {
        if (!isEconomyEnabled()) return String.valueOf(amount);
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    public boolean hasEnough(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.has.failed", e.getMessage()));
            return false;
        }
    }

    // 获取经济实例（高级用法）
    public Economy getEconomy() {
        return economy;
    }
}
