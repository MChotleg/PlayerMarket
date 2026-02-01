package org.playermarket;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.playermarket.commands.MarketCommand;
import org.playermarket.commands.SellCommand;
import org.playermarket.commands.PurCommand;
import org.playermarket.listeners.MarketListener;
import org.playermarket.gui.MarketGUI;
import org.bukkit.plugin.java.JavaPlugin;
import org.playermarket.economy.EconomyManager;
import org.playermarket.database.DatabaseManager;

public class PlayerMarket extends JavaPlugin {
    private MarketListener marketListener;
    private static PlayerMarket instance;
    private MarketGUI marketGUI;
    private MarketCommand marketCommandExecutor;
    private SellCommand sellCommandExecutor;
    private PurCommand purCommandExecutor;
    private EconomyManager economyManager;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§a[PlayerMarket] 插件启动中...");

        try {
            // 1. 先加载配置
            initConfig();

            // 2. 初始化经济系统
            economyManager = new EconomyManager(this);

            // 3. 初始化市场GUI
            marketGUI = new MarketGUI(this);

            // 4. 初始化数据库（必须在命令之前）
            initDatabase();

            // 5. 初始化监听器
            initListeners();

            // 6. 创建命令执行器并注册
            marketCommandExecutor = new MarketCommand(this);
            sellCommandExecutor = new SellCommand(this);
            purCommandExecutor = new PurCommand(this);
            registerCommandSync();

            // 7. 检查经济系统状态
            if (economyManager.isEconomyEnabled()) {
                getLogger().info("经济系统已连接: " + economyManager.getProviderName());
            } else {
                getLogger().warning("经济系统不可用，市场交易功能将受限");
            }

            getLogger().info("玩家市场系统准备就绪!");

        } catch (Exception e) {
            getLogger().severe("插件启动失败: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    /**
     * 同步注册命令（在主线程中直接注册）
     */
    private void registerCommandSync() {
        try {
            // 注册 playermarket 命令
            PluginCommand marketCommand = getCommand("playermarket");
            if (marketCommand != null) {
                marketCommand.setExecutor(marketCommandExecutor);
                marketCommand.setTabCompleter(marketCommandExecutor);
                getLogger().info("命令 /playermarket 已成功注册");
            } else {
                getLogger().severe("无法找到命令：playermarket，请检查 plugin.yml");
            }
            
            // 注册 manuela 命令
            PluginCommand sellCommand = getCommand("manuela");
            if (sellCommand != null) {
                sellCommand.setExecutor(sellCommandExecutor);
                getLogger().info("命令 /manuela 已成功注册");
            } else {
                getLogger().severe("无法找到命令：manuela，请检查 plugin.yml");
            }
            
            // 注册 pur 命令
            PluginCommand purCommand = getCommand("pur");
            if (purCommand != null) {
                purCommand.setExecutor(purCommandExecutor);
                getLogger().info("命令 /pur 已成功注册");
            } else {
                getLogger().severe("无法找到命令：pur，请检查 plugin.yml");
            }
        } catch (Exception e) {
            getLogger().severe("命令注册失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("插件正在关闭...");

        // 0. 关闭所有玩家的自定义GUI界面
        closeAllPlayerGUIs();

        // 1. 清理命令（同步执行）
        cleanupCommands();

        // 2. 清理事件监听器
        cleanupListeners();

        // 4. 清理静态实例
        instance = null;

        // 5. 清理数据库连接
        if (databaseManager != null) {
            databaseManager.closeConnection();
            databaseManager = null;
        }

        // 7. 清理其他资源
        economyManager = null;
        marketGUI = null;
        marketCommandExecutor = null;
        sellCommandExecutor = null;
        purCommandExecutor = null;

        getLogger().info("§c[PlayerMarket] 插件已完全清理");
    }

    /**
     * 清理命令
     */
    private void cleanupCommands() {
        try {
            // 注销 playermarket 命令
            PluginCommand marketCommand = getCommand("playermarket");
            if (marketCommand != null) {
                marketCommand.setExecutor(null);
                marketCommand.setTabCompleter(null);
                getLogger().info("命令 /playermarket 已注销");
            }
            
            // 注销 manuela 命令
            PluginCommand sellCommand = getCommand("manuela");
            if (sellCommand != null) {
                sellCommand.setExecutor(null);
                getLogger().info("命令 /manuela 已注销");
            }
        } catch (Exception e) {
            getLogger().warning("命令注销时发生错误: " + e.getMessage());
        }
    }

    /**
     * 关闭所有玩家的自定义GUI界面
     */
    private void closeAllPlayerGUIs() {
        if (marketListener != null) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (player.getOpenInventory() != null) {
                    Object holder = player.getOpenInventory().getTopInventory().getHolder();
                    if (holder instanceof org.playermarket.gui.MarketGUI ||
                        holder instanceof org.playermarket.gui.MarketItemsGUI ||
                        holder instanceof org.playermarket.gui.BuyOrderGUI ||
                        holder instanceof org.playermarket.gui.BuyOrderDetailGUI ||
                        holder instanceof org.playermarket.gui.MyBuyOrdersGUI ||
                        holder instanceof org.playermarket.gui.ModifyBuyOrderGUI ||
                        holder instanceof org.playermarket.gui.ItemDetailGUI ||
                        holder instanceof org.playermarket.gui.MyListingsGUI ||
                        holder instanceof org.playermarket.gui.DelistingDetailGUI ||
                        holder instanceof org.playermarket.gui.WarehouseGUI) {
                        player.closeInventory();
                    }
                }
            });
            getLogger().info("已关闭所有玩家的自定义GUI界面");
        }
    }
    
    /**
     * 清理事件监听器
     */
    private void cleanupListeners() {
        if (marketListener != null) {
            HandlerList.unregisterAll(marketListener);
            marketListener = null;
            getLogger().info("事件监听器已注销");
        }
    }

    private void initConfig() {
        saveDefaultConfig();
        reloadConfig();
        getLogger().info("配置文件已加载");
    }

    private void initListeners() {
        marketListener = new MarketListener(this);
        getServer().getPluginManager().registerEvents(marketListener, this);
        getLogger().info("事件监听器已注册");
    }

    private void initDatabase() {
        // 初始化数据库连接
        databaseManager = new DatabaseManager(this);
        getLogger().info("数据库系统已初始化");
    }

    public static PlayerMarket getInstance() {
        return instance;
    }

    public MarketGUI getMarketGUI() {
        return marketGUI;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
