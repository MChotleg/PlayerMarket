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

public class DelistingDetailGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final MarketItem marketItem;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    
    private final Map<UUID, Integer> playerDelistAmounts = new HashMap<>();
    private final Map<UUID, Integer> playerMultiplier = new HashMap<>();
    private static final int DEFAULT_MULTIPLIER = 1;
    private static final int MAX_MULTIPLIER = 1000;
    private static final int MIN_MULTIPLIER = 1;
    
    private final Map<UUID, Boolean> playerUpdating = new HashMap<>();
    
    public DelistingDetailGUI(PlayerMarket plugin, MarketItem marketItem, Player player) {
        this.plugin = plugin;
        this.marketItem = marketItem;
        this.player = player;
        this.title = "§6商品下架 §7| §eID: " + marketItem.getId();
        this.inventory = Bukkit.createInventory(this, 54, title);
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        inventory.clear();
        
        addDecorativeBorder();
        
        ItemStack itemStack = marketItem.getItemStack();
        inventory.setItem(13, itemStack);
        
        addInfoButtons();
        addQuantityControls();
        addDelistButtons();
    }
    
    private void addDecorativeBorder() {
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }
        
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            inventory.setItem(slot, borderItem);
        }
    }
    
    private void addInfoButtons() {
        ItemStack priceButton = createInfoButton(
            "§6§l价格信息",
            Material.GOLD_BLOCK,
            "§7库存: §e" + marketItem.getAmount() + " 个",
            "§7单价: §e" + plugin.getEconomyManager().format(marketItem.getUnitPrice()) + " / 个",
            "§7总价: §e" + plugin.getEconomyManager().format(marketItem.getPrice())
        );
        inventory.setItem(11, priceButton);
        
        ItemStack timeButton = createInfoButton(
            "§6§l上架信息",
            Material.CLOCK,
            "§7上架时间: §e" + formatTime(marketItem.getListTime()),
            "§7商品ID: §e" + marketItem.getId()
        );
        inventory.setItem(15, timeButton);
    }
    
    private void addQuantityControls() {
        ItemStack amountDisplay = createAmountDisplay();
        inventory.setItem(31, amountDisplay);
        
        ItemStack multiplierDisplay = createMultiplierDisplay();
        inventory.setItem(40, multiplierDisplay);
        
        ItemStack increaseButton = createControlButton(
            "§a§l增加数量",
            Material.LIME_STAINED_GLASS_PANE,
            "§7点击增加下架数量",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(30, increaseButton);
        
        ItemStack decreaseButton = createControlButton(
            "§c§l减少数量",
            Material.RED_STAINED_GLASS_PANE,
            "§7点击减少下架数量",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(32, decreaseButton);
        
        ItemStack increaseMultiplierButton = createControlButton(
            "§a§l×10",
            Material.LIME_WOOL,
            "§7点击将基数乘以10",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        ItemStack decreaseMultiplierButton = createControlButton(
            "§c§l÷10",
            Material.RED_WOOL,
            "§7点击将基数除以10",
            "§7当前基数: §e" + DEFAULT_MULTIPLIER
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addDelistButtons() {
        ItemStack delistOneButton = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = delistOneButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e§l下架指定数量");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e数量: §f" + getDelistAmount());
            meta.setLore(lore);
            delistOneButton.setItemMeta(meta);
        }
        inventory.setItem(48, delistOneButton);
        
        ItemStack delistAllButton = new ItemStack(Material.DIAMOND);
        ItemMeta allMeta = delistAllButton.getItemMeta();
        if (allMeta != null) {
            allMeta.setDisplayName("§c§l下架全部商品");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7数量: §f" + marketItem.getAmount());
            allMeta.setLore(lore);
            delistAllButton.setItemMeta(allMeta);
        }
        inventory.setItem(50, delistAllButton);
        
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        
        if (backMeta != null) {
            backMeta.setDisplayName("§c§l返回");
            
            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add("§7点击返回我的上架界面");
            backMeta.setLore(backLore);
            
            backButton.setItemMeta(backMeta);
        }
        
        inventory.setItem(45, backButton);
    }
    
    private ItemStack createAmountDisplay() {
        ItemStack display = new ItemStack(Material.PAPER);
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l下架数量");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e1 §7个");
            lore.add("");
            lore.add("§7最大可下架: §e" + marketItem.getAmount() + " 个");
            lore.add("");
            lore.add("§e点击调整数量");
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }
    
    private ItemStack createMultiplierDisplay() {
        ItemStack display = new ItemStack(Material.COMPASS);
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6§l数量基数");
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e1");
            lore.add("");
            lore.add("§7每次增加/减少的数量");
            lore.add("");
            lore.add("§e点击调整基数");
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
    
    public void open() {
        playerDelistAmounts.put(player.getUniqueId(), 1);
        playerMultiplier.put(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void updateDisplay() {
        playerUpdating.put(player.getUniqueId(), true);
        
        int amount = playerDelistAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        ItemStack amountDisplay = inventory.getItem(31);
        if (amountDisplay != null) {
            ItemMeta meta = amountDisplay.getItemMeta();
            if (meta != null) {
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§e" + amount + " §7个");
                lore.add("");
                lore.add("§7最大可下架: §e" + marketItem.getAmount() + " 个");
                meta.setLore(lore);
                amountDisplay.setItemMeta(meta);
                inventory.setItem(31, amountDisplay);
            }
        }
        
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
        
        updateButtonLore(30, multiplier);
        updateButtonLore(32, multiplier);
        updateButtonLore(39, multiplier);
        updateButtonLore(41, multiplier);
        updateDelistButton(48, amount);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
            playerUpdating.remove(player.getUniqueId());
        });
    }
    
    private void updateDelistButton(int slot, int amount) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 5) {
                lore.set(4, "§e数量: §f" + amount);
                meta.setLore(lore);
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
        }
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
    
    private void updatePriceButtonLore(int slot, double priceMultiplier) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 2) {
                lore.set(1, "§7当前基数: §e" + priceMultiplier);
                meta.setLore(lore);
                item.setItemMeta(meta);
                inventory.setItem(slot, item);
            }
        }
    }
    
    public void increaseAmount() {
        int amount = playerDelistAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.min(amount + multiplier, marketItem.getAmount());
        
        if (newAmount != amount) {
            playerDelistAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    
    public void decreaseAmount() {
        int amount = playerDelistAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.max(amount - multiplier, 1);
        
        if (newAmount != amount) {
            playerDelistAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    
    public void increaseMultiplier() {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.min(multiplier * 10, MAX_MULTIPLIER);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    
    public void decreaseMultiplier() {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.max(multiplier / 10, MIN_MULTIPLIER);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    
    public int getDelistAmount() {
        return playerDelistAmounts.getOrDefault(player.getUniqueId(), 1);
    }
    
    public MarketItem getMarketItem() {
        return marketItem;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void cleanup() {
        if (playerUpdating.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        playerDelistAmounts.remove(player.getUniqueId());
        playerMultiplier.remove(player.getUniqueId());
        playerUpdating.remove(player.getUniqueId());
    }
}