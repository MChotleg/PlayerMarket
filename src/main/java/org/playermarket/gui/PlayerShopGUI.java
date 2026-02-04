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
import org.playermarket.model.BuyOrder;
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerShopGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final Player player;
    private final DatabaseManager dbManager;
    private final Inventory inventory;
    
    public PlayerShopGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.dbManager = plugin.getDatabaseManager();
        this.inventory = Bukkit.createInventory(this, 54, I18n.get(player, "market.player_shop.title"));
        createInventory();
    }
    
    private void createInventory() {
        // Fill with black stained glass panes as background
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        
        // 推荐店铺区域标题（槽位4）
        ItemStack featuredTitle = new ItemStack(Material.OAK_SIGN);
        ItemMeta titleMeta = featuredTitle.getItemMeta();
        if (titleMeta != null) {
            titleMeta.setDisplayName(I18n.get(player, "player_shop.featured.title"));
            List<String> titleLore = new ArrayList<>();
            titleLore.add(I18n.get(player, "player_shop.featured.lore"));
            titleMeta.setLore(titleLore);
            featuredTitle.setItemMeta(titleMeta);
        }
        inventory.setItem(4, featuredTitle);
        
        // 显示推荐店铺（使用虚拟数据）
        displayFeaturedShops();
        
        // 查看全服店铺按钮（槽位53）
        ItemStack allShopsButton = new ItemStack(Material.COMPASS);
        ItemMeta allShopsMeta = allShopsButton.getItemMeta();
        if (allShopsMeta != null) {
            allShopsMeta.setDisplayName(I18n.get(player, "player_shop.all_shops.button"));
            List<String> allShopsLore = new ArrayList<>();
            allShopsLore.add(I18n.get(player, "player_shop.all_shops.lore"));
            allShopsMeta.setLore(allShopsLore);
            allShopsButton.setItemMeta(allShopsMeta);
        }
        inventory.setItem(53, allShopsButton);
        
        // Back button (slot 45)
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));
            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add(I18n.get(player, "gui.back.to.main"));
            backMeta.setLore(backLore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(45, backButton);
        
        // 玩家头颅信息槽位（槽位49）
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerHeadMeta = playerHead.getItemMeta();
        if (playerHeadMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) playerHeadMeta;
            skullMeta.setOwningPlayer(player);
            skullMeta.setDisplayName(I18n.get(player, "market.player_shop"));
            List<String> headLore = new ArrayList<>();
            headLore.add(I18n.get(player, "player_shop.your_shop.lore"));
            String balanceText = I18n.get(player, "command.balance",
                    plugin.getEconomyManager().format(plugin.getEconomyManager().getBalance(player)));
            headLore.add(balanceText);
            skullMeta.setLore(headLore);
            playerHead.setItemMeta(skullMeta);
        }
        inventory.setItem(49, playerHead);
    }
    
    private void displayFeaturedShops() {
        // 从配置读取推荐店铺UUID列表
        List<String> featuredShopUUIDs = plugin.getConfig().getStringList("player-shops.featured-shops");
        
        // 从配置读取推荐店铺显示数量，默认14个（最大槽位数）
        int maxDisplayCount = plugin.getConfig().getInt("player-shops.gui.featured-shops-display-count", 14);
        
        // 在中间区域显示推荐店铺（槽位19-25, 28-34）
        int[] featuredSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        
        // 确保显示数量不超过可用槽位数量
        int slotIndex = 0;
        int displayedCount = 0;
        
        for (int i = 0; i < featuredShopUUIDs.size() && displayedCount < maxDisplayCount && slotIndex < featuredSlots.length; i++) {
            String uuidString = featuredShopUUIDs.get(i);
            try {
                UUID playerUuid = UUID.fromString(uuidString);
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
                
                // 检查玩家是否存在
                if (offlinePlayer.getName() == null) {
                    // 玩家不存在，跳过
                    plugin.getLogger().warning("推荐店铺玩家不存在，UUID: " + uuidString);
                    continue;
                }
                
                String playerName = offlinePlayer.getName();
                
                // 获取实际的上架商品数量
                int listingsCount = dbManager.getPlayerListingsCount(playerUuid);
                
                // 获取实际的求购订单数量
                List<BuyOrder> buyOrders = dbManager.getPlayerBuyOrders(playerUuid);
                int buyOrdersCount = (int) buyOrders.stream().filter(order -> !order.isFulfilled()).count();
                
                // 如果玩家没有活跃的商品或订单，可以选择跳过（可选）
                // 但为了显示推荐店铺，即使没有活跃商品也显示
                
                ItemStack shopItem = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta shopMeta = shopItem.getItemMeta();
                
                if (shopMeta instanceof SkullMeta) {
                    SkullMeta skullMeta = (SkullMeta) shopMeta;
                    skullMeta.setOwningPlayer(offlinePlayer);
                    
                    // 使用玩家名称作为店铺名称
                    String shopName = I18n.get(player, "player_shop.featured.default_name", playerName);
                    skullMeta.setDisplayName("§b§l" + shopName);
                    
                    List<String> lore = new ArrayList<>();
                    lore.add(I18n.get(player, "player_shop.featured.default_description", playerName));
                    lore.add("");
                    lore.add("§a§l" + I18n.get(player, "player_shop.featured.listings") + ": §e" + listingsCount);
                    lore.add("§6§l" + I18n.get(player, "player_shop.featured.buy_orders") + ": §e" + buyOrdersCount);
                    lore.add("");
                    lore.add(I18n.get(player, "player_shop.featured.click_hint"));
                    skullMeta.setLore(lore);
                    shopItem.setItemMeta(skullMeta);
                    inventory.setItem(featuredSlots[slotIndex], shopItem);
                    
                    slotIndex++;
                    displayedCount++;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的推荐店铺UUID: " + uuidString);
            }
        }
        
        // 如果没有显示任何推荐店铺（要么没配置，要么玩家都不存在），显示提示信息
        if (displayedCount == 0) {
            ItemStack noShopsItem = new ItemStack(Material.BARRIER);
            ItemMeta noShopsMeta = noShopsItem.getItemMeta();
            if (noShopsMeta != null) {
                noShopsMeta.setDisplayName(I18n.get(player, "player_shop.featured.no_shops"));
                List<String> lore = new ArrayList<>();
                lore.add(I18n.get(player, "player_shop.featured.no_shops_lore"));
                noShopsMeta.setLore(lore);
                noShopsItem.setItemMeta(noShopsMeta);
                inventory.setItem(22, noShopsItem); // 在中心位置显示提示
            }
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