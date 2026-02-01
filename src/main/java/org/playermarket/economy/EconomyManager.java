package org.playermarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.playermarket.PlayerMarket;

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
            // 检查Vault是否存在
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                plugin.getLogger().warning("Vault 未安装，经济功能将禁用");
                return false;
            }

            // 获取经济服务提供者
            RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                    .getServicesManager().getRegistration(Economy.class);

            if (rsp == null) {
                plugin.getLogger().warning("未找到经济服务提供者");
                return false;
            }

            economy = rsp.getProvider();
            providerName = economy.getName();
            enabled = true;
            plugin.getLogger().info("经济系统已连接: " + providerName);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("经济系统初始化失败: " + e.getMessage());
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

    // 获取玩家余额
    public double getBalance(Player player) {
        if (!isEconomyEnabled()) return 0.0;
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().warning("获取玩家余额失败: " + e.getMessage());
            return 0.0;
        }
    }
    
    // 获取玩家余额（支持UUID，玩家可以不在线）
    public double getBalance(java.util.UUID playerUuid) {
        if (!isEconomyEnabled()) return 0.0;
        try {
            return economy.getBalance(Bukkit.getOfflinePlayer(playerUuid));
        } catch (Exception e) {
            plugin.getLogger().warning("获取玩家余额失败: " + e.getMessage());
            return 0.0;
        }
    }

    // 存款
    public boolean deposit(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            economy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("存款操作失败: " + e.getMessage());
            return false;
        }
    }
    
    // 存款（支持UUID，玩家可以不在线）
    public boolean deposit(java.util.UUID playerUuid, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            economy.depositPlayer(Bukkit.getOfflinePlayer(playerUuid), amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("存款操作失败: " + e.getMessage());
            return false;
        }
    }

    // 取款（检查余额是否足够）
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
            plugin.getLogger().warning("取款操作失败: " + e.getMessage());
            return false;
        }
    }
    
    // 取款（支持UUID，玩家可以不在线）
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
            plugin.getLogger().warning("取款操作失败: " + e.getMessage());
            return false;
        }
    }

    // 格式化金额（显示货币符号）
    public String format(double amount) {
        if (!isEconomyEnabled()) return String.valueOf(amount);
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.valueOf(amount);
        }
    }

    // 检查是否有足够余额
    public boolean hasEnough(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().warning("检查余额失败: " + e.getMessage());
            return false;
        }
    }

    // 获取经济实例（高级用法）
    public Economy getEconomy() {
        return economy;
    }
}
