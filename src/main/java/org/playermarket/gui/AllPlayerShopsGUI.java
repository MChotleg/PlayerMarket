package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.database.DatabaseManager;
import org.playermarket.model.PlayerShop;
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AllPlayerShopsGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final Player player;
    private final DatabaseManager dbManager;
    private Inventory inventory;
    private int currentPage = 1;
    private final int shopsPerPage = 45;
    private List<PlayerShop> playerShops = new ArrayList<>();
    
    public AllPlayerShopsGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dbManager = plugin.getDatabaseManager();
        createInventory();
        loadPlayerShops();
    }
    
    private void loadPlayerShops() {
        playerShops.clear();
        
        // 从数据库获取有活跃商品或订单的玩家
        List<Map<String, Object>> activePlayers = dbManager.getActivePlayerShops();
        
        for (Map<String, Object> playerInfo : activePlayers) {
            UUID playerUuid = (UUID) playerInfo.get("uuid");
            String playerName = (String) playerInfo.get("name");
            int listingsCount = (int) playerInfo.get("listings_count");
            int buyOrdersCount = (int) playerInfo.get("buy_orders_count");
            
            // 检查玩家是否存在（能获取到离线玩家信息）
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            if (offlinePlayer.getName() == null) {
                // 玩家不存在，跳过
                continue;
            }
            
            // 创建店铺，使用默认格式"玩家名字的店铺"
            PlayerShop shop = new PlayerShop(
                playerUuid, 
                playerName,
                I18n.get(player, "player_shop.featured.default_name", playerName),
                I18n.get(player, "player_shop.featured.default_description", playerName)
            );
            shop.setTotalListings(listingsCount);
            shop.setTotalBuyOrders(buyOrdersCount);
            
            // 检查是否为推荐店铺（从配置读取）
            List<String> featuredShopUUIDs = plugin.getConfig().getStringList("player-shops.featured-shops");
            boolean isRecommended = featuredShopUUIDs.contains(playerUuid.toString());
            shop.setRecommended(isRecommended);
            
            playerShops.add(shop);
        }
        
        refresh();
    }
    
    private void createInventory() {
        int totalPages = (playerShops.size() + shopsPerPage - 1) / shopsPerPage;
        inventory = Bukkit.createInventory(this, 54, I18n.get(player, "player_shop.all_shops.title", currentPage));
        refresh();
    }
    
    public void refresh() {
        // 清空库存
        inventory.clear();
        
        // 填充黑色玻璃板背景
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        
        // 填充玩家店铺
        int startIndex = (currentPage - 1) * shopsPerPage;
        int endIndex = Math.min(startIndex + shopsPerPage, playerShops.size());
        
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            PlayerShop shop = playerShops.get(i);
            inventory.setItem(slot, createShopItem(shop));
            slot++;
            
            // 每行9个物品，跳过最后一行（用于按钮）
            if (slot % 9 == 0 && slot >= 45) {
                break;
            }
        }
        
        // 分页按钮区域（最后一行）
        int totalPages = (playerShops.size() + shopsPerPage - 1) / shopsPerPage;
        
        // 上一页按钮（槽位45）
        if (currentPage > 1) {
            ItemStack prevButton = createPageButton(I18n.get(player, "gui.page.previous"), Material.ARROW, I18n.get(player, "gui.page.previous.lore", currentPage - 1));
            inventory.setItem(45, prevButton);
        }
        
        // 返回按钮（槽位49）- 返回玩家商店主界面
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));
            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add(I18n.get(player, "player_shop.all_shops.back.lore"));
            backMeta.setLore(backLore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(49, backButton);
        
        // 下一页按钮（槽位53）
        if (currentPage < totalPages) {
            ItemStack nextButton = createPageButton(I18n.get(player, "gui.page.next"), Material.ARROW, I18n.get(player, "gui.page.next.lore", currentPage + 1));
            inventory.setItem(53, nextButton);
        }
        
        // 当前页码信息（槽位50）
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta pageMeta = pageInfo.getItemMeta();
        if (pageMeta != null) {
            pageMeta.setDisplayName(I18n.get(player, "gui.page.current", currentPage, totalPages));
            List<String> pageLore = new ArrayList<>();
            pageLore.add(I18n.get(player, "player_shop.all_shops.count", playerShops.size()));
            pageMeta.setLore(pageLore);
            pageInfo.setItemMeta(pageMeta);
        }
        inventory.setItem(50, pageInfo);
    }
    
    private ItemStack createShopItem(PlayerShop shop) {
        ItemStack shopItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta shopMeta = shopItem.getItemMeta();
        
        if (shopMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) shopMeta;
            
            // 尝试获取玩家头颅
            try {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(shop.getPlayerUuid());
                skullMeta.setOwningPlayer(offlinePlayer);
            } catch (Exception e) {
                // 如果无法获取，使用默认玩家名称
                skullMeta.setOwner(shop.getPlayerName());
            }
            
            // 设置显示名称和描述
            String displayName = shop.isRecommended() ? "§6§l[推荐] §b" + shop.getShopName() : "§b§l" + shop.getShopName();
            skullMeta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7店主: §f" + shop.getPlayerName());
            lore.add("§7" + shop.getDescription());
            lore.add("");
            lore.add("§a§l上架商品: §e" + shop.getTotalListings());
            lore.add("§6§l求购订单: §e" + shop.getTotalBuyOrders());
            lore.add("");
            lore.add("§7状态: " + (shop.isOpen() ? "§a营业中" : "§c已关闭"));
            lore.add("");
            lore.add("§e§l点击查看店铺详情");
            
            skullMeta.setLore(lore);
            shopItem.setItemMeta(skullMeta);
        }
        
        return shopItem;
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
    
    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            refresh();
        }
    }
    
    public void nextPage() {
        int totalPages = (playerShops.size() + shopsPerPage - 1) / shopsPerPage;
        if (currentPage < totalPages) {
            currentPage++;
            refresh();
        }
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * 清理GUI资源
     */
    public void cleanup() {
        // 当前没有需要清理的资源，保留方法以保持一致性
    }
}