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
import org.playermarket.utils.I18n;

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
        this.title = String.format(I18n.get(player, "delisting.title"), marketItem.getId());
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
            I18n.get(player, "delisting.price_info"),
            Material.GOLD_BLOCK,
            String.format(I18n.get(player, "delisting.stock"), marketItem.getAmount()),
            String.format(I18n.get(player, "delisting.unit_price"), plugin.getEconomyManager().format(marketItem.getUnitPrice())),
            String.format(I18n.get(player, "delisting.total_price"), plugin.getEconomyManager().format(marketItem.getPrice()))
        );
        inventory.setItem(11, priceButton);
        
        ItemStack timeButton = createInfoButton(
            I18n.get(player, "delisting.list_info"),
            Material.CLOCK,
            String.format(I18n.get(player, "delisting.list_time"), formatTime(marketItem.getListTime())),
            String.format(I18n.get(player, "delisting.item_id"), marketItem.getId())
        );
        inventory.setItem(15, timeButton);
    }
    
    private void addQuantityControls() {
        ItemStack amountDisplay = createAmountDisplay();
        inventory.setItem(31, amountDisplay);
        
        ItemStack multiplierDisplay = createMultiplierDisplay();
        inventory.setItem(40, multiplierDisplay);
        
        ItemStack increaseButton = createControlButton(
            I18n.get(player, "delisting.increase_amount"),
            Material.LIME_STAINED_GLASS_PANE,
            I18n.get(player, "delisting.increase.lore"),
            String.format(I18n.get(player, "delisting.multiplier_current"), DEFAULT_MULTIPLIER)
        );
        inventory.setItem(30, increaseButton);
        
        ItemStack decreaseButton = createControlButton(
            I18n.get(player, "delisting.decrease_amount"),
            Material.RED_STAINED_GLASS_PANE,
            I18n.get(player, "delisting.decrease.lore"),
            String.format(I18n.get(player, "delisting.multiplier_current"), DEFAULT_MULTIPLIER)
        );
        inventory.setItem(32, decreaseButton);
        
        ItemStack increaseMultiplierButton = createControlButton(
            I18n.get(player, "delisting.multiply_10"),
            Material.LIME_WOOL,
            I18n.get(player, "delisting.click_multiply_10"),
            String.format(I18n.get(player, "delisting.multiplier_current"), DEFAULT_MULTIPLIER)
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        ItemStack decreaseMultiplierButton = createControlButton(
            I18n.get(player, "delisting.divide_10"),
            Material.RED_WOOL,
            I18n.get(player, "delisting.click_divide_10"),
            String.format(I18n.get(player, "delisting.multiplier_current"), DEFAULT_MULTIPLIER)
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addDelistButtons() {
        ItemStack delistOneButton = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = delistOneButton.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "delisting.one"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "delisting.one.lore"), getDelistAmount()));
            meta.setLore(lore);
            delistOneButton.setItemMeta(meta);
        }
        inventory.setItem(48, delistOneButton);
        
        ItemStack delistAllButton = new ItemStack(Material.DIAMOND);
        ItemMeta allMeta = delistAllButton.getItemMeta();
        if (allMeta != null) {
            allMeta.setDisplayName(I18n.get(player, "delisting.all"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "delisting.all.lore"), marketItem.getAmount()));
            allMeta.setLore(lore);
            delistAllButton.setItemMeta(allMeta);
        }
        inventory.setItem(50, delistAllButton);
        
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        
        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));
            
            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add(I18n.get(player, "delisting.back_lore"));
            backMeta.setLore(backLore);
            
            backButton.setItemMeta(backMeta);
        }
        
        inventory.setItem(45, backButton);
    }
    
    private ItemStack createAmountDisplay() {
        ItemStack display = new ItemStack(Material.PAPER);
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "delisting.amount_title"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "delisting.amount_current"), 1));
            lore.add("");
            lore.add(String.format(I18n.get(player, "delisting.amount_max"), marketItem.getAmount()));
            lore.add("");
            lore.add(I18n.get(player, "delisting.click_adjust"));
            meta.setLore(lore);
            display.setItemMeta(meta);
        }
        return display;
    }
    
    private ItemStack createMultiplierDisplay() {
        ItemStack display = new ItemStack(Material.COMPASS);
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "delisting.multiplier_title"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "delisting.multiplier_current"), 1));
            lore.add("");
            lore.add(I18n.get(player, "delisting.multiplier_desc"));
            lore.add("");
            lore.add(I18n.get(player, "delisting.multiplier_adjust"));
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
            return String.format(I18n.get(player, "time.days_ago"), days);
        } else if (hours > 0) {
            return String.format(I18n.get(player, "time.hours_ago"), hours);
        } else if (minutes > 0) {
            return String.format(I18n.get(player, "time.minutes_ago"), minutes);
        } else {
            return I18n.get(player, "time.just_now");
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
                List<String> lore = meta.getLore();
                if (lore != null && lore.size() >= 4) {
                    lore.set(1, String.format(I18n.get(player, "delisting.amount_current"), amount));
                    lore.set(3, String.format(I18n.get(player, "delisting.amount_max"), marketItem.getAmount()));
                    meta.setLore(lore);
                    amountDisplay.setItemMeta(meta);
                    inventory.setItem(31, amountDisplay);
                }
            }
        }
        
        ItemStack multiplierDisplay = inventory.getItem(40);
        if (multiplierDisplay != null) {
            ItemMeta meta = multiplierDisplay.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore != null && lore.size() >= 4) {
                    lore.set(1, String.format(I18n.get(player, "delisting.multiplier_current"), multiplier));
                    lore.set(3, I18n.get(player, "delisting.multiplier_desc"));
                    meta.setLore(lore);
                    multiplierDisplay.setItemMeta(meta);
                    inventory.setItem(40, multiplierDisplay);
                }
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
            if (lore != null && lore.size() >= 2) {
                lore.set(1, String.format(I18n.get(player, "delisting.one.lore"), amount));
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
                lore.set(1, String.format(I18n.get(player, "delisting.multiplier_current"), multiplier));
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
                lore.set(1, String.format(I18n.get(player, "delisting.multiplier_current"), priceMultiplier));
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
