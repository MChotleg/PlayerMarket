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
    private final Map<Integer, PlayerShop> shopSlotMap = new HashMap<>();
    
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
            int salesCount = playerInfo.containsKey("sales_count") ? (int) playerInfo.get("sales_count") : 0;
            int purchasesCount = playerInfo.containsKey("purchases_count") ? (int) playerInfo.get("purchases_count") : 0;
            double salesAmount = playerInfo.containsKey("sales_amount") ? (double) playerInfo.get("sales_amount") : 0.0;
            double purchasesAmount = playerInfo.containsKey("purchases_amount") ? (double) playerInfo.get("purchases_amount") : 0.0;
            
            // 检查玩家是否存在（能获取到离线玩家信息）
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
            if (offlinePlayer.getName() == null) {
                // 玩家不存在，跳过
                continue;
            }
            
            // 检查是否为推荐店铺（从配置读取）
            List<String> featuredShopUUIDs = plugin.getConfig().getStringList("player-shops.featured-shops");
            boolean isRecommended = featuredShopUUIDs.contains(playerUuid.toString());

            String nameKey = isRecommended ? "player_shop.featured.default_name" : "player_shop.detail.shop_name";
            String descKey = isRecommended ? "player_shop.featured.default_description" : "player_shop.default_description";
            
            // 创建店铺
            PlayerShop shop = new PlayerShop(
                playerUuid, 
                playerName,
                I18n.get(player, nameKey, playerName),
                I18n.get(player, descKey, playerName)
            );
            shop.setTotalListings(listingsCount);
            shop.setTotalBuyOrders(buyOrdersCount);
            shop.setTotalSales(salesCount);
            shop.setTotalPurchases(purchasesCount);
            shop.setTotalSalesAmount(salesAmount);
            shop.setTotalPurchasesAmount(purchasesAmount);
            
            shop.setRecommended(isRecommended);

            boolean isOpen = plugin.getConfig().getBoolean("player-shops.open-status." + playerUuid.toString(), true);
            shop.setOpen(isOpen);
            
            playerShops.add(shop);
        }
        
        // 排序逻辑：推荐 > 交易总金额(销售额+收购额) > 营业状态
        playerShops.sort((s1, s2) -> {
            // 1. 推荐店铺优先
            if (s1.isRecommended() != s2.isRecommended()) {
                return s1.isRecommended() ? -1 : 1;
            }
            
            // 2. 交易总金额（销售额 + 收购额）优先
            double amount1 = s1.getTotalSalesAmount() + s1.getTotalPurchasesAmount();
            double amount2 = s2.getTotalSalesAmount() + s2.getTotalPurchasesAmount();
            if (amount1 != amount2) {
                return Double.compare(amount2, amount1); // 降序
            }
            
            // 3. 营业状态优先（营业中 > 打烊）
            if (s1.isOpen() != s2.isOpen()) {
                return s1.isOpen() ? -1 : 1;
            }
            
            // 4. 兜底：按商品数量排序
            return Integer.compare(s2.getTotalListings(), s1.getTotalListings());
        });
        
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
            
            // 存储槽位到店铺的映射
            shopSlotMap.put(slot, shop);
            
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
            String displayName = shop.isRecommended() ? "§6§l[" + I18n.get(player, "gui.recommended") + "] §b" + shop.getShopName() : "§b§l" + shop.getShopName();
            skullMeta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7" + I18n.get(player, "player_shop.owner") + ": §f" + shop.getPlayerName());
            lore.add("§7" + shop.getDescription());
            lore.add("");
            lore.add("§a§l" + I18n.get(player, "player_shop.listings") + ": §e" + shop.getTotalListings());
            lore.add("§6§l" + I18n.get(player, "player_shop.buy_orders") + ": §e" + shop.getTotalBuyOrders());
            lore.add("§b§l" + I18n.get(player, "player_shop.sales_amount") + ": §e" + String.format("%.2f", shop.getTotalSalesAmount()));
            lore.add("§d§l" + I18n.get(player, "player_shop.purchases_amount") + ": §e" + String.format("%.2f", shop.getTotalPurchasesAmount()));
            lore.add("");
            lore.add("§7" + I18n.get(player, "player_shop.status") + ": " + (shop.isOpen() ? "§a" + I18n.get(player, "player_shop.status.open") : "§c" + I18n.get(player, "player_shop.status.closed")));
            lore.add("");
            lore.add("§e§l" + I18n.get(player, "player_shop.click_to_view"));
            
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
    
    public PlayerShop getShopBySlot(int slot) {
        return shopSlotMap.get(slot);
    }
}