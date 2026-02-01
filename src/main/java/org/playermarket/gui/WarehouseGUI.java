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
import org.playermarket.model.WarehouseItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WarehouseGUI implements InventoryHolder {
    private int requestSeq = 0;
    private final PlayerMarket plugin;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    
    private final Map<UUID, Integer> playerPages = new HashMap<>();
    private final Map<UUID, List<WarehouseItem>> currentPageItems = new HashMap<>();
    private final int ITEMS_PER_PAGE = 45;
    private final int PAGE_BUTTON_START = 45;
    
    public WarehouseGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.title = "§6我的仓库";
        this.inventory = Bukkit.createInventory(this, 54, title);
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        openPage(1);
    }
    
    public void openPage(int page) {
        playerPages.put(player.getUniqueId(), page);

        int seq = ++requestSeq;

        inventory.clear();
        addDecorativeBorder();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            DatabaseManager dbManager = plugin.getDatabaseManager();
            List<WarehouseItem> items = dbManager.getWarehouseItems(player.getUniqueId(), page, ITEMS_PER_PAGE);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (seq != requestSeq) {
                    return;
                }
                currentPageItems.put(player.getUniqueId(), items);

                for (int i = 0; i < items.size(); i++) {
                    WarehouseItem item = items.get(i);
                    ItemStack itemStack = item.getItemStack();
                    itemStack.setAmount(1);

                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        List<String> lore = new ArrayList<>();
                        lore.add("");
                        lore.add("§7数量: §e" + item.getAmount());
                        lore.add("");
                        lore.add("§e点击取出物品");
                        lore.add("§7仓库物品ID: §e" + item.getId());
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);
                    }

                    if (i < ITEMS_PER_PAGE) {
                        inventory.setItem(i, itemStack);
                    }
                }

                addPageButtons(page);
                addNavigationButtons();
            });
        });
    }
    
    private void addDecorativeBorder() {
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }
        
        int[] borderSlots = {45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            inventory.setItem(slot, borderItem);
        }
    }
    
    private void addPageButtons(int currentPage) {
        DatabaseManager dbManager = plugin.getDatabaseManager();
        int totalItems = dbManager.getWarehouseItemsCount(player.getUniqueId());
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        
        if (currentPage > 1) {
            ItemStack prevButton = createPageButton("§c§l上一页", Material.ARROW, "§7点击返回第 " + (currentPage - 1) + " 页");
            inventory.setItem(45, prevButton);
        }
        
        ItemStack pageIndicator = createPageIndicator(currentPage, totalPages, totalItems);
        inventory.setItem(49, pageIndicator);
        
        if (currentPage < totalPages) {
            ItemStack nextButton = createPageButton("§a§l下一页", Material.ARROW, "§7点击前往第 " + (currentPage + 1) + " 页");
            inventory.setItem(53, nextButton);
        }
    }
    
    private void addNavigationButtons() {
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l返回");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7点击返回主菜单");
            backMeta.setLore(lore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(48, backButton);
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
    
    private ItemStack createPageIndicator(int currentPage, int totalPages, int totalItems) {
        ItemStack indicator = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = indicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l第 " + currentPage + " 页");
            List<String> lore = new ArrayList<>();
            lore.add("§7共 " + totalPages + " 页");
            lore.add("");
            lore.add("§7仓库物品: §e" + totalItems + " 个");
            lore.add("");
            lore.add("§7点击刷新");
            meta.setLore(lore);
            indicator.setItemMeta(meta);
        }
        return indicator;
    }
    
    public void open() {
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void nextPage() {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        DatabaseManager dbManager = plugin.getDatabaseManager();
        int totalPages = (int) Math.ceil((double) dbManager.getWarehouseItemsCount(player.getUniqueId()) / ITEMS_PER_PAGE);
        
        if (currentPage < totalPages) {
            openPage(currentPage + 1);
        }
    }
    
    public void previousPage() {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        if (currentPage > 1) {
            openPage(currentPage - 1);
        }
    }
    
    public void refresh() {
        int currentPage = playerPages.getOrDefault(player.getUniqueId(), 1);
        openPage(currentPage);
    }
    
    public WarehouseItem getItemBySlot(int slot) {
        List<WarehouseItem> items = currentPageItems.get(player.getUniqueId());
        if (items != null && slot >= 0 && slot < items.size()) {
            return items.get(slot);
        }
        return null;
    }
    
    public int getCurrentPage() {
        return playerPages.getOrDefault(player.getUniqueId(), 1);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void cleanup() {
        playerPages.remove(player.getUniqueId());
        currentPageItems.remove(player.getUniqueId());
    }
}