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
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class BuyOrderDetailGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final BuyOrder buyOrder;
    private final Player player;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    private Inventory inventory;
    
    private static final Map<UUID, Integer> playerSellAmounts = new HashMap<>();
    private static final Map<UUID, Integer> playerMultiplier = new HashMap<>();
    private static final Map<UUID, Boolean> playerUpdating = new HashMap<>();
    private static final int DEFAULT_MULTIPLIER = 1;
    
    public BuyOrderDetailGUI(PlayerMarket plugin, BuyOrder buyOrder, Player player) {
        if (plugin == null || buyOrder == null || player == null) {
            throw new IllegalArgumentException("BuyOrderDetailGUI: plugin, buyOrder and player must not be null");
        }
        this.plugin = plugin;
        this.buyOrder = buyOrder;
        this.player = player;
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        playerSellAmounts.putIfAbsent(player.getUniqueId(), 1);
        playerMultiplier.putIfAbsent(player.getUniqueId(), DEFAULT_MULTIPLIER);
        createInventory();
    }
    
    private void createInventory() {
        inventory = Bukkit.createInventory(this, 54, I18n.get(player, "buy_order_detail.title"));
        
        // 显示收购订单信息
        ItemStack orderInfo = createOrderInfoItem();
        inventory.setItem(13, orderInfo);
        
        // 添加信息按钮
        addInfoButtons();
        
        // 数量选择按钮
        addQuantityControls();
        
        // 出售按钮
        addSellButton();
        
        // 用黑色玻璃板填充空格子
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        int[] emptySlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 33, 34, 35, 36, 37, 38, 42, 43, 44, 46, 47, 48, 50, 51, 52, 53};
        for (int slot : emptySlots) {
            inventory.setItem(slot, blackPane);
        }
        
        // 添加返回按钮（槽位 45）
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();

        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));

            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add(I18n.get(player, "buy_order_detail.back_to_buy_market"));
            backMeta.setLore(backLore);

            backButton.setItemMeta(backMeta);
        }

        inventory.setItem(45, backButton);
    }
    
    private ItemStack createOrderInfoItem() {
        ItemStack item = buyOrder.getItemStack();
        if (item == null) {
            item = new ItemStack(org.bukkit.Material.BARRIER);
        }
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.buyer"), buyOrder.getBuyerName()));
            lore.add(String.format(I18n.get(player, "buy_order_detail.amount_remaining"), buyOrder.getRemainingAmount(), buyOrder.getAmount()));
            lore.add(String.format(I18n.get(player, "buy_order_detail.price_unit"), economyManager.format(buyOrder.getUnitPrice())));
            lore.add(String.format(I18n.get(player, "buy_order_detail.total_remaining"), economyManager.format(buyOrder.getRemainingTotalPrice())));
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.order_id"), buyOrder.getId()));
            lore.add(String.format(I18n.get(player, "buy_order_detail.create_time"), formatTime(buyOrder.getCreateTime())));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
    
    private void addInfoButtons() {
        ItemStack buyerInfoButton = createInfoButton(
            I18n.get(player, "buy_order_detail.buyer_info_title"),
            Material.PLAYER_HEAD,
            String.format(I18n.get(player, "buy_order_detail.buyer"), buyOrder.getBuyerName()),
            String.format(I18n.get(player, "buy_order_detail.order_id"), buyOrder.getId()),
            String.format(I18n.get(player, "buy_order_detail.create_time"), formatTime(buyOrder.getCreateTime()))
        );
        inventory.setItem(11, buyerInfoButton);

        ItemStack itemStack = buyOrder.getItemStack();
        String itemName = itemStack != null ? getItemDisplayName(itemStack) : "未知物品";

        ItemStack itemInfoButton = createInfoButton(
            I18n.get(player, "buy_order_detail.item_info_title"),
            Material.CHEST,
            String.format(I18n.get(player, "buy_order_detail.item_name"), itemName),
            String.format(I18n.get(player, "buy_order_detail.price_unit"), economyManager.format(buyOrder.getUnitPrice()))
        );
        inventory.setItem(15, itemInfoButton);
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
            I18n.get(player, "buy_order_detail.increase_amount"),
            Material.GREEN_STAINED_GLASS_PANE,
            I18n.get(player, "buy_order_detail.click_to_increase"),
            String.format(I18n.get(player, "buy_order_detail.current_multiplier"), playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(30, increaseButton);
        
        // 减少数量按钮（槽位 32）
        ItemStack decreaseButton = createControlButton(
            I18n.get(player, "buy_order_detail.decrease_amount"),
            Material.RED_STAINED_GLASS_PANE,
            I18n.get(player, "buy_order_detail.click_to_decrease"),
            String.format(I18n.get(player, "buy_order_detail.current_multiplier"), playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(32, decreaseButton);
        
        // 增加基数按钮（槽位 39）
        ItemStack increaseMultiplierButton = createControlButton(
            I18n.get(player, "buy_order_detail.multiply_10"),
            Material.LIME_WOOL,
            I18n.get(player, "buy_order_detail.click_multiply_10"),
            String.format(I18n.get(player, "buy_order_detail.current_multiplier"), playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        // 减少基数按钮（槽位 41）
        ItemStack decreaseMultiplierButton = createControlButton(
            I18n.get(player, "buy_order_detail.divide_10"),
            Material.RED_WOOL,
            I18n.get(player, "buy_order_detail.click_divide_10"),
            String.format(I18n.get(player, "buy_order_detail.current_multiplier"), playerMultiplier.get(player.getUniqueId()))
        );
        inventory.setItem(41, decreaseMultiplierButton);
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
            int currentAmount = playerSellAmounts.get(player.getUniqueId());
            double totalIncome = currentAmount * buyOrder.getUnitPrice();
            int maxAmount = Math.min(buyOrder.getRemainingAmount(), getPlayerItemAmount());
            
            meta.setDisplayName(I18n.get(player, "buy_order_detail.sell_amount_title"));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.amount_current"), currentAmount));
            lore.add(String.format(I18n.get(player, "buy_order_detail.amount_max"), maxAmount));
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.income"), economyManager.format(totalIncome)));
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

            meta.setDisplayName(I18n.get(player, "buy_order_detail.multiplier_title"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.current_multiplier"), multiplier));
            lore.add("");
            lore.add(I18n.get(player, "buy_order_detail.multiplier_desc"));
            lore.add("");
            lore.add(I18n.get(player, "buy_order_detail.multiplier_adjust"));
            meta.setLore(lore);

            display.setItemMeta(meta);
        }

        return display;
    }

    private String getItemDisplayName(ItemStack itemStack) {
        return I18n.getItemDisplayName(itemStack);
    }
    
    private void addSellButton() {
        ItemStack sellButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = sellButton.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "buy_order_detail.confirm_sell"));
            
            List<String> lore = new ArrayList<>();
            int sellAmount = playerSellAmounts.get(player.getUniqueId());
            double totalPrice = sellAmount * buyOrder.getUnitPrice();
            
            lore.add("");
            lore.add(String.format(I18n.get(player, "buy_order_detail.sell_amount"), sellAmount));
            lore.add(String.format(I18n.get(player, "buy_order_detail.price_unit"), economyManager.format(buyOrder.getUnitPrice())));
            lore.add(String.format(I18n.get(player, "buy_order_detail.income"), economyManager.format(totalPrice)));
            meta.setLore(lore);
            
            sellButton.setItemMeta(meta);
        }
        
        inventory.setItem(49, sellButton);
    }
    
    private String getDisplayName(ItemStack item) {
        return I18n.getItemDisplayName(item);
    }
    
    private int getPlayerItemAmount() {
        ItemStack targetItem = buyOrder.getItemStack();
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(targetItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }
    
    public void increaseAmount() {
        int amount = playerSellAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int maxAmount = Math.min(buyOrder.getRemainingAmount(), getPlayerItemAmount());
        int newAmount = Math.min(amount + multiplier, maxAmount);
        
        if (newAmount != amount) {
            playerSellAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay();
        }
    }
    
    public void decreaseAmount() {
        int amount = playerSellAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newAmount = Math.max(amount - multiplier, 1);
        
        if (newAmount != amount) {
            playerSellAmounts.put(player.getUniqueId(), newAmount);
            updateDisplay();
        }
    }
    
    public void increaseMultiplier() {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.min(multiplier * 10, 1000);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay();
        }
    }
    
    public void decreaseMultiplier() {
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        int newMultiplier = Math.max(multiplier / 10, 1);
        
        if (newMultiplier != multiplier) {
            playerMultiplier.put(player.getUniqueId(), newMultiplier);
            updateDisplay();
        }
    }
    
    private String formatTime(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return I18n.get(player, "time.just_now");
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(I18n.get(player, "gui.date_format"));
        return sdf.format(new java.util.Date(timestamp.getTime()));
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    public void refresh() {
        playerUpdating.put(player.getUniqueId(), true);
        createInventory();
        player.openInventory(inventory);
        playerUpdating.put(player.getUniqueId(), false);
    }
    
    public void updateDisplay() {
        // 设置更新标志，防止cleanup被调用
        playerUpdating.put(player.getUniqueId(), true);
        
        int amount = playerSellAmounts.getOrDefault(player.getUniqueId(), 1);
        int multiplier = playerMultiplier.getOrDefault(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        // 更新数量显示
        ItemStack amountDisplay = inventory.getItem(31);
        if (amountDisplay != null) {
            ItemMeta meta = amountDisplay.getItemMeta();
            if (meta != null) {
                double totalIncome = amount * buyOrder.getUnitPrice();
                int maxAmount = Math.min(buyOrder.getRemainingAmount(), getPlayerItemAmount());
                
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add(String.format(I18n.get(player, "buy_order_detail.amount_current"), amount));
                lore.add(String.format(I18n.get(player, "buy_order_detail.amount_max"), maxAmount));
                lore.add("");
                lore.add(String.format(I18n.get(player, "buy_order_detail.income"), economyManager.format(totalIncome)));
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
                lore.add(String.format(I18n.get(player, "buy_order_detail.current_multiplier"), multiplier));
                lore.add("");
                lore.add(I18n.get(player, "buy_order_detail.multiplier_desc"));
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
            playerUpdating.put(player.getUniqueId(), false);
        });
    }
    
    private void updateButtonLore(int slot, int multiplier) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 2) {
                lore.set(1, String.format(I18n.get(player, "buy_order_detail.current_multiplier"), multiplier));
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
        
        playerSellAmounts.remove(player.getUniqueId());
        playerMultiplier.remove(player.getUniqueId());
        playerUpdating.remove(player.getUniqueId());
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public BuyOrder getBuyOrder() {
        return buyOrder;
    }
    
    public static Map<UUID, Integer> getPlayerSellAmounts() {
        return playerSellAmounts;
    }
    
    public static Map<UUID, Integer> getPlayerMultiplier() {
        return playerMultiplier;
    }
    
    public static Map<UUID, Boolean> getPlayerUpdating() {
        return playerUpdating;
    }
}