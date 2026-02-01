package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.model.BuyOrder;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class MyBuyOrdersGUI implements InventoryHolder {
    private int requestSeq = 0;
    private final PlayerMarket plugin;
    private final Player player;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    private Inventory inventory;
    private int currentPage = 1;
    private final int itemsPerPage = 28;
    private List<BuyOrder> buyOrders = new ArrayList<>();
    
    private final Map<UUID, BuyOrderDetailGUI> playerDetailGUIs = new HashMap<>();
    
    public MyBuyOrdersGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        createInventory();
        updateBuyOrdersAsync(() -> {
            createInventory();
            player.openInventory(inventory);
        });
    }
    
    private void updateBuyOrders() {
        buyOrders = dbManager.getPlayerBuyOrders(player.getUniqueId());
    }

    private void updateBuyOrdersAsync(Runnable after) {
        int seq = ++requestSeq;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<BuyOrder> orders = dbManager.getPlayerBuyOrders(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (seq != requestSeq) {
                    return;
                }
                buyOrders = orders;
                if (after != null) {
                    after.run();
                }
            });
        });
    }
    
    private void createInventory() {
        int totalPages = (buyOrders.size() + itemsPerPage - 1) / itemsPerPage;
        inventory = Bukkit.createInventory(this, 54, "§6我的收购订单 - 第 " + currentPage + " 页");
        
        // 填充收购订单
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, buyOrders.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            BuyOrder buyOrder = buyOrders.get(i);
            inventory.setItem(slot++, createBuyOrderItem(buyOrder));
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
        
        // 返回按钮
        ItemStack backButton = createPageButton("§c§l返回", Material.BARRIER, "§7返回上一个界面");
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
        
        // 填充其他空格子
        int[] emptySlots = {47, 48, 50, 51, 52};
        for (int slotIdx : emptySlots) {
            inventory.setItem(slotIdx, blackPane);
        }
    }
    
    private ItemStack createBuyOrderItem(BuyOrder buyOrder) {
        ItemStack item = buyOrder.getItemStack();
        item.setAmount(1); // 强制设置为1，隐藏数字角标
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // 保留原有的显示名称
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7收购数量: §e" + buyOrder.getAmount() + " 个");
            lore.add("§7剩余数量: §e" + buyOrder.getRemainingAmount() + " 个");
            lore.add("§7单价: §e" + economyManager.format(buyOrder.getUnitPrice()) + " / 个");
            lore.add("§7总金额: §e" + economyManager.format(buyOrder.getTotalPrice()));
            lore.add("§7剩余金额: §e" + economyManager.format(buyOrder.getRemainingTotalPrice()));
            lore.add("");
            int acquiredAmount = buyOrder.getAmount() - buyOrder.getRemainingAmount();
            boolean isPartiallyFulfilled = acquiredAmount > 0 && !buyOrder.isFulfilled();
            
            if (buyOrder.isFulfilled()) {
                // 已完成
                lore.add("§7状态: §a已完成");
                if (buyOrder.getSellerName() != null) {
                    lore.add("§7卖家: §e" + buyOrder.getSellerName());
                }
                lore.add("§a左键: 转移物品到仓库");
                lore.add("§c右键: 删除订单");
            } else if (isPartiallyFulfilled) {
                // 部分完成
                lore.add("§7状态: §e部分完成");
                lore.add("§7已收购: §e" + acquiredAmount + " 个");
                lore.add("");
                lore.add("§a左键: 转移已收购物品到仓库");
                lore.add("§c右键: 取消剩余收购");
            } else {
                // 未完成
                lore.add("§a左键: 修改收购数量");
                lore.add("§c右键: 取消收购订单");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
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
            lore.add("§7点击刷新");
            meta.setLore(lore);
            indicator.setItemMeta(meta);
        }
        
        return indicator;
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void refresh() {
        updateBuyOrdersAsync(() -> {
            createInventory();
            player.openInventory(inventory);
        });
    }
    
    public void refreshPage(Player player) {
        if (player.getInventory().getHolder() == inventory.getHolder()) {
            refresh();
        }
    }
    
    public void cleanup() {
        // 清理资源
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public void setCurrentPage(int page) {
        this.currentPage = page;
    }
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public List<BuyOrder> getBuyOrders() {
        return buyOrders;
    }
    
    public Map<UUID, BuyOrderDetailGUI> getPlayerDetailGUIs() {
        return playerDetailGUIs;
    }
}