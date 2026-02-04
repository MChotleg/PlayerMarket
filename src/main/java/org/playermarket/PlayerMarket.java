package org.playermarket;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.playermarket.commands.MarketCommand;
import org.playermarket.commands.SellCommand;
import org.playermarket.commands.PurCommand;
import org.playermarket.listeners.InventoryClickListener;
import org.playermarket.listeners.PlayerJoinListener;
import org.playermarket.gui.MarketGUI;
import org.bukkit.plugin.java.JavaPlugin;
import org.playermarket.economy.EconomyManager;
import org.playermarket.database.DatabaseManager;
import org.playermarket.utils.I18n;
import org.playermarket.Metrics;
import org.playermarket.utils.UpdateChecker;

public class PlayerMarket extends JavaPlugin {
    private InventoryClickListener inventoryClickListener;
    private PlayerJoinListener playerJoinListener;
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
        getLogger().info(I18n.get("plugin.starting"));

        try {
            initConfig();

            I18n.initialize(this);

            economyManager = new EconomyManager(this);

            marketGUI = new MarketGUI(this);

            initDatabase();

            initListeners();

            // bStats Metrics
            int pluginId = 29299; // TODO: 替换为你的bStats插件ID
            Metrics metrics = new Metrics(this, pluginId);

            //统计插件版本分布
            metrics.addCustomChart(
            new Metrics.SimplePie("plugin_version", 
                () -> this.getDescription().getVersion())
            );

            //统计经济提供商使用情况
            metrics.addCustomChart(
                new Metrics.SimplePie("economy_provider",
                    () -> economyManager.getProviderName())
            );


            //统计语言设置（使用饼图）
            metrics.addCustomChart(
                new Metrics.SimplePie("default_language",
                    () -> getConfig().getString("language", "en_US"))
            );

            marketCommandExecutor = new MarketCommand(this);
            sellCommandExecutor = new SellCommand(this);
            purCommandExecutor = new PurCommand(this);
            registerCommandSync();

            if (economyManager.isEconomyEnabled()) {
                getLogger().info(I18n.get("plugin.economy.connected", economyManager.getProviderName()));
            } else {
                getLogger().warning(I18n.get("plugin.economy.unavailable"));
            }

            try {
                if (getConfig().getBoolean("updates.enabled", true) && getConfig().getBoolean("updates.check-at-start", true)) {
                    new UpdateChecker(this).checkAsync();
                }
            } catch (Exception ignored) {}

            getLogger().info(I18n.get("plugin.ready"));

        } catch (Exception e) {
            getLogger().severe(I18n.get("plugin.startup.failed", e.getMessage()));
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    /**
     * 同步注册命令（在主线程中直接注册）
     */
    private void registerCommandSync() {
        try {
            PluginCommand marketCommand = getCommand("playermarket");
            if (marketCommand != null) {
                marketCommand.setExecutor(marketCommandExecutor);
                marketCommand.setTabCompleter(marketCommandExecutor);
                getLogger().info(I18n.get("plugin.command.registered", "/playermarket"));
            } else {
                getLogger().severe(I18n.get("plugin.command.notfound", "playermarket"));
            }
            
            PluginCommand sellCommand = getCommand("manuela");
            if (sellCommand != null) {
                sellCommand.setExecutor(sellCommandExecutor);
                getLogger().info(I18n.get("plugin.command.registered", "/manuela"));
            } else {
                getLogger().severe(I18n.get("plugin.command.notfound", "manuela"));
            }
            
            PluginCommand purCommand = getCommand("pur");
            if (purCommand != null) {
                purCommand.setExecutor(purCommandExecutor);
                getLogger().info(I18n.get("plugin.command.registered", "/pur"));
            } else {
                getLogger().severe(I18n.get("plugin.command.notfound", "pur"));
            }
        } catch (Exception e) {
            getLogger().severe(I18n.get("plugin.command.failed", e.getMessage()));
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(I18n.get("plugin.shutdown"));

        closeAllPlayerGUIs();

        cleanupListeners();

        instance = null;

        if (databaseManager != null) {
            databaseManager.closeConnection();
            databaseManager = null;
        }

        economyManager = null;
        marketGUI = null;
        marketCommandExecutor = null;
        sellCommandExecutor = null;
        purCommandExecutor = null;

        getLogger().info(I18n.get("plugin.cleaned"));
    }

    /**
     * 关闭所有玩家的自定义GUI界面
     */
    private void closeAllPlayerGUIs() {
        if (inventoryClickListener != null) {
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
                        holder instanceof org.playermarket.gui.WarehouseGUI ||
                        holder instanceof org.playermarket.gui.AllPlayerShopsGUI ||
                        holder instanceof org.playermarket.gui.PlayerShopGUI ||
                        holder instanceof org.playermarket.gui.PlayerShopDetailGUI ||
                        holder instanceof org.playermarket.gui.PlayerShopSettingsGUI) {
                        player.closeInventory();
                    }
                }
            });
            getLogger().info(I18n.get("plugin.guis.closed"));
        }
    }
    
    private void cleanupListeners() {
        if (inventoryClickListener != null) {
            HandlerList.unregisterAll(inventoryClickListener);
            inventoryClickListener = null;
            getLogger().info(I18n.get("plugin.listeners.unregistered"));
        }
        if (playerJoinListener != null) {
            HandlerList.unregisterAll(playerJoinListener);
            playerJoinListener = null;
        }
    }

    private void initConfig() {
        saveDefaultConfig();
        reloadConfig();
        getLogger().info(I18n.get("plugin.config.loaded"));
    }

    private void initListeners() {
        inventoryClickListener = new InventoryClickListener(this);
        playerJoinListener = new PlayerJoinListener(this);
        getServer().getPluginManager().registerEvents(inventoryClickListener, this);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        getLogger().info(I18n.get("plugin.listeners.registered"));
    }

    private void initDatabase() {
        databaseManager = new DatabaseManager(this);
        getLogger().info(I18n.get("plugin.database.initialized"));
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
