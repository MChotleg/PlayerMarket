package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.model.MarketItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemDetailGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final MarketItem marketItem;
    private final Inventory inventory;
    private final String title;
    
    // 购买数量控制
    private final Map<UUID, Integer> playerPurchaseAmounts = new HashMap<>();
    private final Map<UUID, Integer> playerMultiplier = new HashMap<>();
    private static final int DEFAULT_MULTIPLIER = 1;
    private static final int MAX_MULTIPLIER = 1000;
    private static final int MIN_MULTIPLIER = 1;
    
    // 标志位：防止updateDisplay时触发cleanup
    private final Map<UUID, Boolean> playerUpdating = new HashMap<>();
    
    public ItemDetailGUI(PlayerMarket plugin, MarketItem marketItem) {
        this.plugin = plugin;
        this.marketItem = marketItem;
        this.title = "§6商品详情 §7| §eID: " + marketItem.getId();
        this.inventory = Bukkit.createInventory(this, 54, title);
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        inventory.clear();
        
        // 显示原始物品（槽位 13）
        ItemStack itemStack = marketItem.getItemStack();
        inventory.setItem(13, itemStack);
        
        // 添加信息按钮
        addInfoButtons();
        
        // 添加数量控制区域
        addQuantityControls();
        
        // 添加购买按钮
        addBuyButton();
    }
    
    private void addInfoButtons() {
        // 价格信息（槽位 11）
        ItemStack priceButton = createInfoButton(
            "§6§l价格信息",
            Material.GOLD_INGOT,
            "§7库存: §e" + marketItem.getAmount(),
            "§7单价: §e" + plugin.getEconomyManager().format(marketItem.getPrice() / marketItem.getAmount()) + " / 个"
        );
        inventory.setItem(11, priceButton);
        
        // 卖家信息（槽位 15）
        ItemStack sellerButton = createInfoButton(
            "§6§l卖家信息",
            Material.PLAYER_HEAD,
            "§7名称: §e" + marketItem.getSellerName(),
            "§7上架时间: §e" + formatTime(marketItem.getListTime())
        );
        inventory.setItem(15, sellerButton);
    }
    
    private void addQuantityControls() {
        // 数量显示（槽位 31）
        ItemStack amountDisplay = createAmountDisplay();
        inventory.setItem(31, amountDisplay);
        
        // 基数显示（槽位 40）
        ItemStack multiplierDisplay = createMultiplierDisplay();
        inventory.setItem(40, multiplierDisplay);
        
        // 增加数量按钮（槽位 30）
        ItemStack increaseButton = createControlButton(
            "§a§l增加数量",
            Material.GREEN_STAINED_GLASS_PANE,
            "§7点击增加购买数量",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(30, increaseButton);
        
        // 减少数量按钮（槽位 32）
        ItemStack decreaseButton = createControlButton(
            "§c§l减少数量",
            Material.RED_STAINED_GLASS_PANE,
            "§7点击减少购买数量",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(32, decreaseButton);
        
        // 增加基数按钮（槽位 39）
        ItemStack increaseMultiplierButton = createControlButton(
            "§a§l×10",
            Material.LIME_WOOL,
            "§7点击将基数乘以10",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        // 减少基数按钮（槽位 41）
        ItemStack decreaseMultiplierButton = createControlButton(
            "§c§l÷10",
            Material.RED_WOOL,
            "§7点击将基数除以10",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addBuyButton() {
        ItemStack buyButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = buyButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§l确认购买");
            
            List<String> lore = new ArrayList<>();
            meta.setLore(lore);
            
            buyButton.setItemMeta(meta);
        }
        
        inventory.setItem(49, buyButton);
        
        // 用黑色玻璃板填充空格子
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        int[] emptySlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 33, 34, 35, 36, 37, 38, 42, 43, 44, 45, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : emptySlots) {
            inventory.setItem(slot, blackPane);
        }
        
        // 添加返回按钮（槽位 45）
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l返回");
            
            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add("§7点击返回上一页");
            backMeta.setLore(backLore);
            
            backButton.setItemMeta(backMeta);
        }
        
        inventory.setItem(45, backButton);
    }
    
    private void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    private void playSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
    }
    
    private void playErrorSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
    
    private ItemStack createAmountDisplay() {
        ItemStack display = new ItemStack(Material.PAPER);
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l购买数量");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e1 §7个");
            lore.add("");
            lore.add("§7总价: §e" + plugin.getEconomyManager().format(marketItem.getPrice() / marketItem.getAmount()));
            meta.setLore(lore);
            
            display.setItemMeta(meta);
        }
        
        return display;
    }
    
    private ItemStack createMultiplierDisplay() {
        ItemStack display = new ItemStack(Material.COMPASS);
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§6§l基数");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e1");
            lore.add("");
            lore.add("§7每次增加/减少的数量");
            meta.setLore(lore);
            
            display.setItemMeta(meta);
        }
        
        return display;
    }
    
    private ItemStack createControlButton(String name, Material material, String... loreLines) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        
        return button;
    }
    
    private ItemStack createInfoButton(String name, Material material, String... loreLines) {
        ItemStack button = new ItemStack(material);
        ItemMeta meta = button.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        
        return button;
    }
    
    private String formatTime(java.sql.Timestamp timestamp) {
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " 天前";
        } else if (hours > 0) {
            return hours + " 小时前";
        } else if (minutes > 0) {
            return minutes + " 分钟前";
        } else {
            return "刚刚";
        }
    }
    
    public void open(Player player) {
        // 初始化玩家的购买数量和基数
        playerPurchaseAmounts.put(player.getUniqueId(), 1);
        playerMultiplier.put(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void updateDisplay(Player player) {
        // 设置更新标志，防止cleanup被调用
        playerUpdating.put(player.getUniqueId(), true);
        
        int amount = playerPurchaseAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        // 更新数量显示
        ItemStack amountDisplay = inventory.getItem(31);
        if (amountDisplay != null) {
            ItemMeta meta = amountDisplay.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§e" + amount + " §7个");
                lore.add("");
                double totalPrice = (marketItem.getPrice() / marketItem.getAmount()) * amount;
                lore.add("§7总价: §e" + plugin.getEconomyManager().format(totalPrice));
                meta.setLore(lore);
                amountDisplay.setItemMeta(meta);
                inventory.setItem(31, amountDisplay);
            }
        }
        
        // 更新基数显示
        ItemStack multiplierDisplay = inventory.getItem(40);
        if (multiplierDisplay != null) {
            ItemMeta meta = multiplierDisplay.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§e" + multiplier);
                lore.add("");
                lore.add("§7每次增加/减少的数量");
                meta.setLore(lore);
                multiplierDisplay.setItemMeta(meta);
                inventory.setItem(40, multiplierDisplay);
            }
        }
        
        // 更新按钮的基数提示
        updateButtonLore(30, multiplier);
        updateButtonLore(32, multiplier);
        updateButtonLore(39, multiplier);
        updateButtonLore(41, multiplier);
        
        // 重新打开界面以更新显示
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
            // 清除更新标志
            playerUpdating.remove(player.getUniqueId());
        });
    }
    
    private void updateButtonLore(int slot, int multiplier) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 2) {
                lore.set(1, "§7当前基数: §e" + multiplier);
                meta.setLore(lore);
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
        }
    }
    
    public void increaseAmount(Player player) {
        int amount = playerPurchaseAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.min(amount + multiplier, marketItem.getAmount());
        
        if (newAmount != amount) {
            playerPurchaseAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay(player);
            playClickSound(player);
        } else {
            playErrorSound(player);
        }
    }
    
    public void decreaseAmount(Player player) {
        int amount = playerPurchaseAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.max(amount - multiplier, 1);
        
        if (newAmount != amount) {
            playerPurchaseAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay(player);
            playClickSound(player);
        } else {
            playErrorSound(player);
        }
    }
    
    public void increaseMultiplier(Player player) {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.min(multiplier * 10, MAX_MULTIPLIER);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay(player);
            playClickSound(player);
        } else {
            playErrorSound(player);
        }
    }
    
    public void decreaseMultiplier(Player player) {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.max(multiplier / 10, MIN_MULTIPLIER);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay(player);
            playClickSound(player);
        } else {
            playErrorSound(player);
        }
    }
    
    public int getPurchaseAmount(Player player) {
        return playerPurchaseAmounts.getOrDefault(player.getUniqueId(), 1);
    }
    
    public void cleanup(Player player) {
        // 检查是否是因为updateDisplay而关闭的
        if (playerUpdating.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        playerPurchaseAmounts.remove(player.getUniqueId());
        playerMultiplier.remove(player.getUniqueId());
        playerUpdating.remove(player.getUniqueId());
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public MarketItem getMarketItem() {
        return marketItem;
    }
    
    public String getTitle() {
        return title;
    }
}