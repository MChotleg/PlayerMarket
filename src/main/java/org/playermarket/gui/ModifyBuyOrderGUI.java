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
import org.playermarket.model.BuyOrder;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ModifyBuyOrderGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final BuyOrder buyOrder;
    private final Player player;
    private final Inventory inventory;
    private final String title;
    
    private final Map<UUID, Integer> playerNewAmounts = new HashMap<>();
    private final Map<UUID, Integer> playerMultiplier = new HashMap<>();
    private static final int DEFAULT_MULTIPLIER = 1;
    private static final int MAX_MULTIPLIER = 1000;
    private static final int MIN_MULTIPLIER = 1;
    
    private final Map<UUID, Boolean> playerUpdating = new HashMap<>();
    
    public ModifyBuyOrderGUI(PlayerMarket plugin, BuyOrder buyOrder, Player player) {
        this.plugin = plugin;
        this.buyOrder = buyOrder;
        this.player = player;
        this.title = "§6修改收购数量 §7| §eID: " + buyOrder.getId();
        this.inventory = Bukkit.createInventory(this, 54, title);
        
        playerNewAmounts.put(player.getUniqueId(), buyOrder.getAmount());
        playerMultiplier.put(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        initializeGUI();
    }
    
    private void initializeGUI() {
        inventory.clear();
        
        addDecorativeBorder();
        
        ItemStack itemStack = buyOrder.getItemStack();
        itemStack.setAmount(1);
        inventory.setItem(13, itemStack);
        
        addInfoButtons();
        addQuantityControls();
        addBackButton();
        addConfirmButton();
    }
    
    private void addDecorativeBorder() {
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }
        
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 33, 34, 35, 36, 37, 38, 42, 43, 44, 45, 46, 47, 50, 51, 52, 53};
        for (int slot : borderSlots) {
            inventory.setItem(slot, borderItem);
        }
    }
    
    private void addInfoButtons() {
        ItemStack priceButton = createInfoButton(
            "§6§l价格信息",
            Material.GOLD_BLOCK,
            "§7单价: §e" + plugin.getEconomyManager().format(buyOrder.getUnitPrice()) + " / 个",
            "§7当前数量: §e" + buyOrder.getAmount() + " 个",
            "§7剩余数量: §e" + buyOrder.getRemainingAmount() + " 个"
        );
        inventory.setItem(11, priceButton);
        
        ItemStack timeButton = createInfoButton(
            "§6§l订单信息",
            Material.CLOCK,
            "§7订单ID: §e" + buyOrder.getId(),
            "§7创建时间: §e" + formatTime(buyOrder.getCreateTime())
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
            "§7点击增加收购数量",
            "§7当前基数: §e" + playerMultiplier.get(player.getUniqueId())
        );
        inventory.setItem(30, increaseButton);
        
        ItemStack decreaseButton = createControlButton(
            "§c§l减少数量",
            Material.RED_STAINED_GLASS_PANE,
            "§7点击减少收购数量",
            "§7当前基数: §e" + playerMultiplier.get(player.getUniqueId())
        );
        inventory.setItem(32, decreaseButton);
        
        ItemStack increaseMultiplierButton = createControlButton(
            "§a§l×10",
            Material.LIME_WOOL,
            "§7点击将基数乘以10",
            "§7当前基数: §e" + playerMultiplier.get(player.getUniqueId())
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        ItemStack decreaseMultiplierButton = createControlButton(
            "§c§l÷10",
            Material.RED_WOOL,
            "§7点击将基数除以10",
            "§7当前基数: §e" + playerMultiplier.get(player.getUniqueId())
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addBackButton() {
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = backButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§c§l返回");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7返回我的收购界面");
            meta.setLore(lore);
            
            backButton.setItemMeta(meta);
        }
        
        inventory.setItem(48, backButton);
    }
    
    private void addConfirmButton() {
        ItemStack confirmButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = confirmButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§a§l确认修改");
            
            List<String> lore = new ArrayList<>();
            int newAmount = playerNewAmounts.get(player.getUniqueId());
            double newTotalPrice = newAmount * buyOrder.getUnitPrice();
            
            lore.add("");
            lore.add("§7新数量: §e" + newAmount + " 个");
            lore.add("§7单价: §e" + plugin.getEconomyManager().format(buyOrder.getUnitPrice()) + " / 个");
            lore.add("§7新总价: §e" + plugin.getEconomyManager().format(newTotalPrice));
            meta.setLore(lore);
            
            confirmButton.setItemMeta(meta);
        }
        
        inventory.setItem(49, confirmButton);
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
    
    private ItemStack createAmountDisplay() {
        ItemStack display = new ItemStack(Material.PAPER);
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            int currentAmount = playerNewAmounts.get(player.getUniqueId());
            double totalIncome = currentAmount * buyOrder.getUnitPrice();
            
            meta.setDisplayName("§e§l新收购数量");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7当前: §e" + currentAmount + " 个");
            lore.add("");
            lore.add("§7总金额: §e" + plugin.getEconomyManager().format(totalIncome));
            meta.setLore(lore);
            
            display.setItemMeta(meta);
        }
        
        return display;
    }
    
    private ItemStack createMultiplierDisplay() {
        ItemStack display = new ItemStack(Material.COMPASS);
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            int multiplier = playerMultiplier.get(player.getUniqueId());
            
            meta.setDisplayName("§b§l数量倍数");
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§7当前: §e" + multiplier + "x");
            lore.add("");
            lore.add("§7每次增加/减少的数量");
            meta.setLore(lore);
            
            display.setItemMeta(meta);
        }
        
        return display;
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
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    public void refresh() {
        playerUpdating.put(player.getUniqueId(), true);
        initializeGUI();
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
            playerUpdating.put(player.getUniqueId(), false);
        });
    }
    
    public void increaseAmount() {
        int amount = playerNewAmounts.get(player.getUniqueId());
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = amount + multiplier;
        
        if (newAmount != amount) {
            playerNewAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }
    
    public void decreaseAmount() {
        int amount = playerNewAmounts.get(player.getUniqueId());
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.max(amount - multiplier, 1);
        
        if (newAmount != amount) {
            playerNewAmounts.put(player.getUniqueId(), newAmount);
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
    
    private void updateDisplay() {
        playerUpdating.put(player.getUniqueId(), true);
        
        int amount = playerNewAmounts.get(player.getUniqueId());
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        ItemStack amountDisplay = inventory.getItem(31);
        if (amountDisplay != null) {
            ItemMeta meta = amountDisplay.getItemMeta();
            if (meta != null) {
                double totalIncome = amount * buyOrder.getUnitPrice();
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7当前: §e" + amount + " 个");
                lore.add("");
                lore.add("§7总金额: §e" + plugin.getEconomyManager().format(totalIncome));
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
                lore.add("§7当前: §e" + multiplier + "x");
                lore.add("");
                lore.add("§7每次增加/减少的数量");
                meta.setLore(lore);
                multiplierDisplay.setItemMeta(meta);
                inventory.setItem(40, multiplierDisplay);
            }
        }
        
        ItemStack confirmButton = inventory.getItem(48);
        if (confirmButton != null) {
            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                double newTotalPrice = amount * buyOrder.getUnitPrice();
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7新数量: §e" + amount + " 个");
                lore.add("§7单价: §e" + plugin.getEconomyManager().format(buyOrder.getUnitPrice()) + " / 个");
                lore.add("§7新总价: §e" + plugin.getEconomyManager().format(newTotalPrice));
                meta.setLore(lore);
                confirmButton.setItemMeta(meta);
                inventory.setItem(48, confirmButton);
            }
        }
        
        updateButtonLore(30, multiplier);
        updateButtonLore(32, multiplier);
        updateButtonLore(39, multiplier);
        updateButtonLore(41, multiplier);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
            playerUpdating.put(player.getUniqueId(), false);
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
    
    public void cleanup(Player player) {
        if (playerUpdating.getOrDefault(player.getUniqueId(), false)) {
            return;
        }
        
        playerNewAmounts.remove(player.getUniqueId());
        playerMultiplier.remove(player.getUniqueId());
        playerUpdating.remove(player.getUniqueId());
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public BuyOrder getBuyOrder() {
        return buyOrder;
    }
    
    public int getNewAmount() {
        return playerNewAmounts.get(player.getUniqueId());
    }
    
    public String getTitle() {
        return title;
    }
}