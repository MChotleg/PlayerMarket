package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.database.DatabaseManager;
import org.playermarket.model.MarketItem;
import org.playermarket.utils.I18n;

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
        this.marketGUITitle = plugin.getConfig().getString("market.title", I18n.get("market.title"));
        this.holderInventory = Bukkit.createInventory(this, 54, I18n.get("market.title"));
    }
    
    public void openMarketGUI(Player player) {
        openMarketGUI(player, 1);
    }
    
    public void openMarketGUI(Player player, int page) {
        Inventory inventory = createInventory(player);
        playerInventories.put(player.getUniqueId(), inventory);
        
        // 异步打开界面（确保在主线程）
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void open(Player player) {
        openMarketGUI(player, 1);
    }
    
    private Inventory createInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(this, 54, I18n.get(player, "market.title") + " - " + I18n.get(player, "market.main_menu"));

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }

        // Row 1: Main Market Features
        ItemStack buyMarketButton = createPageButton(I18n.get(player, "market.buy"), Material.EMERALD_BLOCK, I18n.get(player, "market.main.buy.lore"));
        inventory.setItem(11, buyMarketButton);

        ItemStack sellMarketButton = createPageButton(I18n.get(player, "market.sell"), Material.DIAMOND_BLOCK, I18n.get(player, "market.main.sell.lore"));
        inventory.setItem(13, sellMarketButton);

        ItemStack playerShopButton = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta playerShopMeta = (SkullMeta) playerShopButton.getItemMeta();
        if (playerShopMeta != null) {
            playerShopMeta.setDisplayName(I18n.get(player, "market.player_shop"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "market.main.player_shop.lore"));
            playerShopMeta.setLore(lore);
            playerShopMeta.setOwningPlayer(player);
            playerShopButton.setItemMeta(playerShopMeta);
        }
        inventory.setItem(15, playerShopButton);

        // Row 3: Personal Features
        ItemStack myListingsButton = createPageButton(I18n.get(player, "market.mylistings"), Material.CHEST, I18n.get(player, "market.main.mylistings.lore"));
        inventory.setItem(29, myListingsButton);

        ItemStack myBuyOrdersButton = createPageButton(I18n.get(player, "market.mybuys"), Material.CHEST, I18n.get(player, "market.main.mybuys.lore"));
        inventory.setItem(31, myBuyOrdersButton);

        ItemStack warehouseButton = createPageButton(I18n.get(player, "market.warehouse"), Material.ENDER_CHEST, I18n.get(player, "market.main.warehouse.lore"));
        inventory.setItem(33, warehouseButton);

        // Row 5: Help
        ItemStack helpBook = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta helpMeta = helpBook.getItemMeta();
        if (helpMeta != null) {
            helpMeta.setDisplayName(I18n.get(player, "market.main.help.title"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "market.main.help.manuela"));
            lore.add(I18n.get(player, "market.main.help.manuela.desc"));
            lore.add("");
            lore.add(I18n.get(player, "market.main.help.pur"));
            lore.add(I18n.get(player, "market.main.help.pur.desc"));
            helpMeta.setLore(lore);
            helpBook.setItemMeta(helpMeta);
        }
        inventory.setItem(49, helpBook);

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
            inventory = createInventory(player);
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
                lore.add(I18n.get(player, "market.item.amount", item.getAmount()));
                lore.add(I18n.get(player, "market.item.total_price", plugin.getEconomyManager().format(item.getPrice())));
                lore.add(I18n.get(player, "market.item.unit_price", plugin.getEconomyManager().format(item.getPrice() / item.getAmount())));
                lore.add("");
                lore.add(I18n.get(player, "market.item.click_detail"));
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
            inventory = createInventory(player);
            playerInventories.put(player.getUniqueId(), inventory);
        }
        
        // 上一页按钮
        if (currentPage > 1) {
            ItemStack prevButton = createPageButton(I18n.get(player, "gui.page.previous"), Material.ARROW, I18n.get(player, "gui.page.previous.lore", currentPage - 1));
            inventory.setItem(45, prevButton);
        } else {
            inventory.setItem(45, blackPane);
        }
        
        // 我的上架按钮
        ItemStack myListingsButton = createPageButton(I18n.get(player, "market.mylistings"), Material.CHEST, I18n.get(player, "market.main.mylistings.lore"));
        inventory.setItem(46, myListingsButton);
        
        // 当前页显示
        ItemStack pageIndicator = createPageIndicator(player, currentPage, totalPages);
        inventory.setItem(49, pageIndicator);
        
        // 下一页按钮
        if (currentPage < totalPages) {
            ItemStack nextButton = createPageButton(I18n.get(player, "gui.page.next"), Material.ARROW, I18n.get(player, "gui.page.next.lore", currentPage + 1));
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
    
    private ItemStack createPageIndicator(Player player, int currentPage, int totalPages) {
        ItemStack indicator = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = indicator.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "gui.page.indicator.title", currentPage));
            
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "gui.page.indicator.total", totalPages));
            lore.add("");
            lore.add(I18n.get(player, "gui.page.indicator.refresh"));
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
