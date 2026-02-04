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
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class PlayerShopListingsGUI implements InventoryHolder {
    private int requestSeq = 0;
    private final PlayerMarket plugin;
    private final Player player;
    private final UUID shopOwnerUuid;
    private final String shopOwnerName;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    private Inventory inventory;
    private int currentPage = 1;
    private final int itemsPerPage = 45;
    private List<MarketItem> marketItems = new ArrayList<>();
    
    private final Map<UUID, ItemDetailGUI> playerDetailGUIs = new HashMap<>();
    
    public PlayerShopListingsGUI(PlayerMarket plugin, Player player, UUID shopOwnerUuid, String shopOwnerName) {
        this.plugin = plugin;
        this.player = player;
        this.shopOwnerUuid = shopOwnerUuid;
        this.shopOwnerName = shopOwnerName;
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        createInventory();
        updateMarketItemsAsync(() -> {
            createInventory();
            player.openInventory(inventory);
        });
    }
    
    public UUID getShopOwnerUuid() {
        return shopOwnerUuid;
    }

    public String getShopOwnerName() {
        return shopOwnerName;
    }

    public List<MarketItem> getMarketItems() {
        return marketItems;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
    }

    private void updateMarketItems() {
        marketItems = dbManager.getPlayerListings(shopOwnerUuid, 1, 10000);
    }

    private void updateMarketItemsAsync(Runnable after) {
        int seq = ++requestSeq;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<MarketItem> items = dbManager.getPlayerListings(shopOwnerUuid, 1, 10000);
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
        if (totalPages == 0) totalPages = 1;
        inventory = Bukkit.createInventory(this, 54, I18n.get(player, "player_shop.listings.title", shopOwnerName, currentPage));
        
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
            ItemStack prevButton = createPageButton(I18n.get(player, "gui.page.previous"), Material.ARROW, I18n.get(player, "gui.page.previous.lore", currentPage - 1));
            inventory.setItem(45, prevButton);
        } else {
            inventory.setItem(45, blackPane);
        }
        
        // 返回按钮（返回店铺详情）
        ItemStack backButton = createPageButton(I18n.get(player, "gui.back"), Material.BARRIER, I18n.get(player, "gui.back.to.shop_detail"));
        inventory.setItem(46, backButton);
        
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
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(I18n.get(player, "marketitems.seller", item.getSellerName()));
            lore.add(I18n.get(player, "marketitems.unit_price", economyManager.format(item.getUnitPrice())));
            lore.add(I18n.get(player, "marketitems.amount", item.getAmount()));
            lore.add(I18n.get(player, "marketitems.total_price", economyManager.format(item.getPrice())));
            lore.add(I18n.get(player, "marketitems.id", item.getId()));
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
    
    private ItemStack createPageIndicator(Player player, int currentPage, int totalPages) {
        ItemStack indicator = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = indicator.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "gui.page.indicator.current_pages"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(I18n.get(player, "gui.page.indicator.current", currentPage));
            lore.add(I18n.get(player, "gui.page.indicator.total_pages", totalPages));
            lore.add("");
            lore.add(I18n.get(player, "gui.page.indicator.refresh"));
            meta.setLore(lore);
            indicator.setItemMeta(meta);
        }
        
        return indicator;
    }
    
    public void open() {
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
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
