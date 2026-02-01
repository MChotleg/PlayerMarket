package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.database.DatabaseManager;
import org.playermarket.model.MarketItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarketGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final String marketGUITitle;
    private final Inventory holderInventory;
    
    // 分页相关
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, List<MarketItem>> currentPageItems = new HashMap<>();
    private final Map<UUID, Inventory> playerInventories = new HashMap<>();
    private final int ITEMS_PER_PAGE = 45; // 每页45个物品（留9个格子给翻页按钮）
    private final int PAGE_BUTTON_START = 45; // 翻页按钮起始位置


    public MarketGUI(PlayerMarket plugin) {
        this.plugin = plugin;
        this.marketGUITitle = plugin.getConfig().getString("market.title", "§6玩家市场");
        this.holderInventory = Bukkit.createInventory(this, 27, "§6玩家市场");
    }
    
    public void openMarketGUI(Player player) {
        openMarketGUI(player, 1);
    }
    
    public void openMarketGUI(Player player, int page) {
        Inventory inventory = createInventory();
        playerInventories.put(player.getUniqueId(), inventory);
        
        // 异步打开界面（确保在主线程）
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void open(Player player) {
        openMarketGUI(player, 1);
    }
    
    private Inventory createInventory() {
        Inventory inventory = Bukkit.createInventory(this, 27, "§6玩家市场 - 主菜单");

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, blackPane);
        }

        ItemStack buyMarketButton = createPageButton("§a§l购买市场", Material.EMERALD_BLOCK, "§7点击进入购买市场");
        inventory.setItem(10, buyMarketButton);

        ItemStack sellMarketButton = createPageButton("§b§l求购市场", Material.DIAMOND_BLOCK, "§7点击进入求购市场");
        inventory.setItem(12, sellMarketButton);

        ItemStack myListingsButton = createPageButton("§e§l我的上架", Material.CHEST, "§7点击查看我的上架物品");
        inventory.setItem(14, myListingsButton);

        ItemStack myBuyOrdersButton = createPageButton("§e§l我的收购", Material.CHEST, "§7点击查看我的收购订单");
        inventory.setItem(16, myBuyOrdersButton);

        ItemStack warehouseButton = createPageButton("§e§l我的仓库", Material.ENDER_CHEST, "§7点击查看我的仓库");
        inventory.setItem(22, warehouseButton);

        ItemStack helpBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta helpMeta = helpBook.getItemMeta();
        if (helpMeta != null) {
            helpMeta.setDisplayName("§6§l玩家市场帮助");
            List<String> lore = new ArrayList<>();
            lore.add("§e/manuela <数量> <单价>");
            lore.add("§7上架手中物品到购买市场");
            lore.add("");
            lore.add("§e/pur <数量> <单价>");
            lore.add("§7发布收购订单（收购物品为手中物品）");
            helpMeta.setLore(lore);
            helpBook.setItemMeta(helpMeta);
        }
        inventory.setItem(4, helpBook);

        return inventory;
    }
    
    private void loadMarketItems(Player player, int page) {
        DatabaseManager dbManager = plugin.getDatabaseManager();
        
        // 从数据库获取商品
        List<MarketItem> items = dbManager.getAvailableItems(page, ITEMS_PER_PAGE);
        
        // 保存当前页面的商品列表（用于点击时获取商品）
        currentPageItems.put(player.getUniqueId(), items);
        
        // 获取玩家的Inventory
        Inventory inventory = playerInventories.get(player.getUniqueId());
        if (inventory == null) {
            inventory = createInventory();
            playerInventories.put(player.getUniqueId(), inventory);
        }
        
        // 清空库存
        inventory.clear();
        
        // 添加商品到GUI（显示原始物品，数量信息在Lore中）
        for (int i = 0; i < items.size(); i++) {
            MarketItem item = items.get(i);
            ItemStack itemStack = item.getItemStack();
            
            // 设置数量为1，避免显示角标
            itemStack.setAmount(1);
            
            // 保留原始物品外观，在Lore中显示数量信息
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7数量: §e" + item.getAmount());
                lore.add("§7价格: §e" + plugin.getEconomyManager().format(item.getPrice()));
                lore.add("§7单价: §e" + plugin.getEconomyManager().format(item.getPrice() / item.getAmount()) + " / 个");
                lore.add("");
                lore.add("§e点击查看详情");
                meta.setLore(lore);
                itemStack.setItemMeta(meta);
            }
            
            // 添加到库存（前45个格子）
            if (i < ITEMS_PER_PAGE) {
                inventory.setItem(i, itemStack);
            }
        }
    }
    
    private void addPageButtons(Player player, int currentPage) {
        DatabaseManager dbManager = plugin.getDatabaseManager();
        int totalPages = (int) Math.ceil((double) dbManager.getTotalAvailableItems() / ITEMS_PER_PAGE);
        
        // 用黑色玻璃板填充空格子
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        // 获取玩家的Inventory
        Inventory inventory = playerInventories.get(player.getUniqueId());
        if (inventory == null) {
            inventory = createInventory();
            playerInventories.put(player.getUniqueId(), inventory);
        }
        
        // 上一页按钮
        if (currentPage > 1) {
            ItemStack prevButton = createPageButton("§c§l上一页", Material.ARROW, "§7点击返回第 " + (currentPage - 1) + " 页");
            inventory.setItem(45, prevButton);
        } else {
            inventory.setItem(45, blackPane);
        }
        
        // 我的上架按钮
        ItemStack myListingsButton = createPageButton("§e§l我的上架", Material.CHEST, "§7点击查看我的上架物品");
        inventory.setItem(46, myListingsButton);
        
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
        for (int slot : emptySlots) {
            inventory.setItem(slot, blackPane);
        }
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
            meta.setDisplayName("§6§l第 " + currentPage + " 页");
            
            List<String> lore = new ArrayList<>();
            lore.add("§7共 " + totalPages + " 页");
            lore.add("");
            lore.add("§7点击刷新");
            meta.setLore(lore);
            
            indicator.setItemMeta(meta);
        }
        
        return indicator;
    }
    
    public void nextPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        DatabaseManager dbManager = plugin.getDatabaseManager();
        int totalPages = (int) Math.ceil((double) dbManager.getTotalAvailableItems() / ITEMS_PER_PAGE);
        
        if (currentPage < totalPages) {
            playerPages.put(player.getUniqueId(), currentPage + 1);
            openMarketGUI(player, currentPage + 1);
        }
    }
    
    public void previousPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        
        if (currentPage > 1) {
            playerPages.put(player.getUniqueId(), currentPage - 1);
            openMarketGUI(player, currentPage - 1);
        }
    }
    
    public void refreshPage(Player player) {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        openMarketGUI(player, currentPage);
    }
    
    @Override
    public Inventory getInventory() {
        return holderInventory;
    }
    
    public Inventory getPlayerInventory(Player player) {
        return playerInventories.get(player.getUniqueId());
    }
    
    public void removePlayerInventory(Player player) {
        playerInventories.remove(player.getUniqueId());
    }
    
    public String getMarketGUITitle() {
        return marketGUITitle;
    }
    
    public int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }
    
    public void open() {
        // 不应该直接调用这个方法，因为它会尝试打开第一个在线玩家的界面
        // 应该使用openMarketGUI(Player player)方法
    }
    
    public void resetPlayerPage(Player player) {
        playerPages.remove(player.getUniqueId());
        currentPageItems.remove(player.getUniqueId());
    }
    
    // 根据槽位获取商品
    public MarketItem getItemBySlot(Player player, int slot) {
        List<MarketItem> items = currentPageItems.get(player.getUniqueId());
        if (items != null && slot >= 0 && slot < items.size()) {
            return items.get(slot);
        }
        return null;
    }
}
