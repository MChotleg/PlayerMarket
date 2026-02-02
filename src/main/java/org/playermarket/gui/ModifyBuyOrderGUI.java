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
        this.title = org.playermarket.utils.I18n.get(player, "modify_buy_order.title", buyOrder.getId());
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
            org.playermarket.utils.I18n.get(player, "modify_buy_order.price_info_title"),
            Material.GOLD_BLOCK,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.price_unit", plugin.getEconomyManager().format(buyOrder.getUnitPrice())),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_current", buyOrder.getAmount()),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_remaining", buyOrder.getRemainingAmount())
        );
        inventory.setItem(11, priceButton);
        
        ItemStack timeButton = createInfoButton(
            org.playermarket.utils.I18n.get(player, "modify_buy_order.order_info_title"),
            Material.CLOCK,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.order_id", buyOrder.getId()),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.create_time", formatTime(buyOrder.getCreateTime()))
        );
        inventory.setItem(15, timeButton);
    }
    
    private void addQuantityControls() {
        ItemStack amountDisplay = createAmountDisplay();
        inventory.setItem(31, amountDisplay);
        
        ItemStack multiplierDisplay = createMultiplierDisplay();
        inventory.setItem(40, multiplierDisplay);
        
        ItemStack increaseButton = createControlButton(
            org.playermarket.utils.I18n.get(player, "modify_buy_order.increase_amount"),
            Material.LIME_STAINED_GLASS_PANE,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.click_to_increase"),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.current_multiplier", playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(30, increaseButton);
        
        ItemStack decreaseButton = createControlButton(
            org.playermarket.utils.I18n.get(player, "modify_buy_order.decrease_amount"),
            Material.RED_STAINED_GLASS_PANE,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.click_to_decrease"),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.current_multiplier", playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(32, decreaseButton);
        
        ItemStack increaseMultiplierButton = createControlButton(
            org.playermarket.utils.I18n.get(player, "modify_buy_order.multiply_10"),
            Material.LIME_WOOL,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.click_multiply_10"),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.current_multiplier", playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        ItemStack decreaseMultiplierButton = createControlButton(
            org.playermarket.utils.I18n.get(player, "modify_buy_order.divide_10"),
            Material.RED_WOOL,
            org.playermarket.utils.I18n.get(player, "modify_buy_order.click_divide_10"),
            org.playermarket.utils.I18n.get(player, "modify_buy_order.current_multiplier", playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addBackButton() {
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta meta = backButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(org.playermarket.utils.I18n.get(player, "gui.back"));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.back_to_my_orders"));
            meta.setLore(lore);
            
            backButton.setItemMeta(meta);
        }
        
        inventory.setItem(48, backButton);
    }
    
    private void addConfirmButton() {
        ItemStack confirmButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = confirmButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(org.playermarket.utils.I18n.get(player, "modify_buy_order.confirm_modify"));
            
            List<String> lore = new ArrayList<>();
            int newAmount = playerNewAmounts.get(player.getUniqueId());
            double newTotalPrice = newAmount * buyOrder.getUnitPrice();
            
            lore.add("");
            lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.new_amount", newAmount));
            lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.price_unit", plugin.getEconomyManager().format(buyOrder.getUnitPrice())));
            lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.new_total_price", plugin.getEconomyManager().format(newTotalPrice)));
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

            meta.setDisplayName(org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_display_title"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_display_current"), currentAmount));
            lore.add("");
            lore.add(String.format(org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_display_total"), plugin.getEconomyManager().format(totalIncome)));
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

            meta.setDisplayName(org.playermarket.utils.I18n.get(player, "modify_buy_order.multiplier_display_title"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(org.playermarket.utils.I18n.get(player, "modify_buy_order.multiplier_display_current"), multiplier));
            lore.add("");
            lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.multiplier_display_desc"));
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
            return org.playermarket.utils.I18n.get(player, "time.days_ago", days);
        } else if (hours > 0) {
            return org.playermarket.utils.I18n.get(player, "time.hours_ago", hours);
        } else if (minutes > 0) {
            return org.playermarket.utils.I18n.get(player, "time.minutes_ago", minutes);
        } else {
            return org.playermarket.utils.I18n.get(player, "time.just_now");
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
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_display_current", amount));
                lore.add("");
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.amount_display_total", plugin.getEconomyManager().format(totalIncome)));
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
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.multiplier_display_current", multiplier));
                lore.add("");
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.multiplier_display_desc"));
                meta.setLore(lore);
                multiplierDisplay.setItemMeta(meta);
                inventory.setItem(40, multiplierDisplay);
            }
        }
        
        ItemStack confirmButton = inventory.getItem(49);
        if (confirmButton != null) {
            ItemMeta meta = confirmButton.getItemMeta();
            if (meta != null) {
                double newTotalPrice = amount * buyOrder.getUnitPrice();
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.new_amount", amount));
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.price_unit", plugin.getEconomyManager().format(buyOrder.getUnitPrice())));
                lore.add(org.playermarket.utils.I18n.get(player, "modify_buy_order.new_total_price", plugin.getEconomyManager().format(newTotalPrice)));
                meta.setLore(lore);
                confirmButton.setItemMeta(meta);
                inventory.setItem(49, confirmButton);
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
                lore.set(1, org.playermarket.utils.I18n.get(player, "modify_buy_order.current_multiplier", multiplier));
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
