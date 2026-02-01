package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.model.MarketItem;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class MarketItemsGUI implements InventoryHolder {
    private int requestSeq = 0;
    private final PlayerMarket plugin;
    private final Player player;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    private Inventory inventory;
    private int currentPage = 1;
    private final int itemsPerPage = 45;
    private List<MarketItem> marketItems = new ArrayList<>();
    
    private final Map<UUID, ItemDetailGUI> playerDetailGUIs = new HashMap<>();
    
    public MarketItemsGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        createInventory();
        updateMarketItemsAsync(() -> {
            createInventory();
            player.openInventory(inventory);
        });
    }
    
    private void updateMarketItems() {
        marketItems = dbManager.getAllUnsoldItems();
    }

    private void updateMarketItemsAsync(Runnable after) {
        int seq = ++requestSeq;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MarketItem> items = dbManager.getAllUnsoldItems();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (seq != requestSeq) {
                    return;
                }
                marketItems = items;
                if (after != null) {
                    after.run();
                }
            });
        });
    }
    
    private void createInventory() {
        int totalPages = (marketItems.size() + itemsPerPage - 1) / itemsPerPage;
        inventory = Bukkit.createInventory(this, 54, "§6购买市场 - 第 " + currentPage + " 页");
        
        // 填充商品
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, marketItems.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            MarketItem item = marketItems.get(i);
            inventory.setItem(slot++, createMarketItem(item));
        }
        
        // 用黑色玻璃板填充空格子
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        // 上一页按钮
        if (currentPage > 1) {
            ItemStack prevButton = createPageButton("§c§l上一页", Material.ARROW, "§7点击返回第 " + (currentPage - 1) + " 页");
            inventory.setItem(45, prevButton);
        } else {
            inventory.setItem(45, blackPane);
        }
        
        // 返回按钮（返回上一页；若已是第一页则返回主菜单）
        ItemStack backButton;
        if (currentPage > 1) {
            backButton = createPageButton("§c§l返回", Material.BARRIER, "§7点击返回上一页");
        } else {
            backButton = createPageButton("§c§l返回", Material.BARRIER, "§7点击返回主菜单");
        }
        inventory.setItem(46, backButton);
        
        // 当前页显示
        ItemStack pageIndicator = createPageIndicator(currentPage, totalPages);
        inventory.setItem(49, pageIndicator);
        
        // 下一页按钮
        if (currentPage < totalPages) {
            ItemStack nextButton = createPageButton("§a§l下一页", Material.ARROW, "§7点击前往第 " + (currentPage + 1) + " 页");
            inventory.setItem(53, nextButton);
        } else {
            inventory.setItem(53, blackPane);
        }
        
        // 用黑色玻璃板填充其他空格子
        int[] emptySlots = {47, 48, 50, 51, 52};
        for (int slotIdx : emptySlots) {
            inventory.setItem(slotIdx, blackPane);
        }
    }
    
    private ItemStack createMarketItem(MarketItem item) {
        ItemStack itemStack = item.getItemStack();
        // 强制将物品数量设置为1，隐藏数字角标
        itemStack.setAmount(1);
        ItemMeta meta = itemStack.getItemMeta();
        
        if (meta != null) {
            // 保留原有的显示名称
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7卖家: §e" + item.getSellerName());
            lore.add("§7单价: §e" + economyManager.format(item.getUnitPrice()) + " / 个");
            lore.add("§7数量: §e" + item.getAmount() + " 个");
            lore.add("§7总价: §e" + economyManager.format(item.getPrice()));
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }
    
    private ItemStack createPageButton(String name, Material material, String lore) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lores = new ArrayList<>();
            lores.add(lore);
            meta.setLore(lores);
            button.setItemMeta(meta);
        }
        
        return button;
    }
    
    private ItemStack createPageIndicator(int currentPage, int totalPages) {
        ItemStack indicator = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = indicator.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l当前页数");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7当前: §e" + currentPage);
            lore.add("§7总页: §e" + totalPages);
            lore.add("");
            lore.add("§a点击: 刷新页面");
            meta.setLore(lore);
            indicator.setItemMeta(meta);
        }
        
        return indicator;
    }
    
    public void open() {
        // Inventory is opened after async data load in constructor
        // This method is kept for compatibility
        if (inventory != null) {
            player.openInventory(inventory);
        }
    }
    
    public void refresh() {
        updateMarketItemsAsync(() -> {
            createInventory();
            player.openInventory(inventory);
        });
    }
    
    public void refreshPage(Player player) {
        if (player.getInventory().getHolder() == inventory.getHolder()) {
            refresh();
        }
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
    
    public List<MarketItem> getMarketItems() {
        return marketItems;
    }
    
    public Map<UUID, ItemDetailGUI> getPlayerDetailGUIs() {
        return playerDetailGUIs;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
}