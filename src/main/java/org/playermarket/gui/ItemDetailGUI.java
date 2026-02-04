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
    
    private Runnable backAction;

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }

    public Runnable getBackAction() {
        return backAction;
    }
    
    public ItemDetailGUI(PlayerMarket plugin, MarketItem marketItem) {
        this.plugin = plugin;
        this.marketItem = marketItem;
        this.title = I18n.get("gui.item.detail") + " §7| §eID: " + marketItem.getId();
        this.inventory = Bukkit.createInventory(this, 54, title);

        initializeGUI();
    }
    
    private void initializeGUI() {
        inventory.clear();
        
        // 显示原始物品（槽位 13）
        ItemStack itemStack = marketItem.getItemStack();
        inventory.setItem(13, itemStack);
        
        // 添加信息按钮
        addInfoButtons(null);
        
        // 添加数量控制区域
        addQuantityControls(null);
        
        // 添加购买按钮
        addBuyButton();
        
        // 添加装饰边框
        addDecorativeBorder();
    }
    
    private void addInfoButtons(Player player) {
        // 价格信息（槽位 11）
        ItemStack priceButton = createInfoButton(
            I18n.get(player, "itemdetail.price_info"),
            Material.GOLD_INGOT,
            I18n.get(player, "itemdetail.stock", marketItem.getAmount()),
            I18n.get(player, "itemdetail.unit_price", plugin.getEconomyManager().format(marketItem.getPrice() / marketItem.getAmount()))
        );
        inventory.setItem(11, priceButton);
        
        // 卖家信息（槽位 15）
        ItemStack sellerButton = createInfoButton(
            I18n.get(player, "itemdetail.seller_info"),
            Material.PLAYER_HEAD,
            I18n.get(player, "itemdetail.seller_name", marketItem.getSellerName()),
            I18n.get(player, "itemdetail.list_time", formatTime(marketItem.getListTime(), player))
        );
        inventory.setItem(15, sellerButton);
    }
    
    private void addQuantityControls(Player player) {
        // 数量显示（槽位 31）
        ItemStack amountDisplay = createAmountDisplay(player);
        inventory.setItem(31, amountDisplay);
        
        // 基数显示（槽位 40）
        ItemStack multiplierDisplay = createMultiplierDisplay(player);
        inventory.setItem(40, multiplierDisplay);
        
        // 增加数量按钮（槽位 30）
        ItemStack increaseButton = createControlButton(
            I18n.get(player, "itemdetail.increase"),
            Material.GREEN_STAINED_GLASS_PANE,
            I18n.get(player, "itemdetail.increase.lore"),
            I18n.get(player, "itemdetail.current_multiplier", DEFAULT_MULTIPLIER)
        );
        inventory.setItem(30, increaseButton);
        
        // 减少数量按钮（槽位 32）
        ItemStack decreaseButton = createControlButton(
            I18n.get(player, "itemdetail.decrease"),
            Material.RED_STAINED_GLASS_PANE,
            I18n.get(player, "itemdetail.decrease.lore"),
            I18n.get(player, "itemdetail.current_multiplier", DEFAULT_MULTIPLIER)
        );
        inventory.setItem(32, decreaseButton);
        
        // 增加基数按钮（槽位 39）
        ItemStack increaseMultiplierButton = createControlButton(
            I18n.get(player, "itemdetail.multiplier_x10"),
            Material.LIME_WOOL,
            I18n.get(player, "itemdetail.multiplier_x10.lore"),
            I18n.get(player, "itemdetail.current_multiplier", DEFAULT_MULTIPLIER)
        );
        inventory.setItem(39, increaseMultiplierButton);
        
        // 减少基数按钮（槽位 41）
        ItemStack decreaseMultiplierButton = createControlButton(
            I18n.get(player, "itemdetail.multiplier_div10"),
            Material.RED_WOOL,
            I18n.get(player, "itemdetail.multiplier_div10.lore"),
            I18n.get(player, "itemdetail.current_multiplier", DEFAULT_MULTIPLIER)
        );
        inventory.setItem(41, decreaseMultiplierButton);
    }
    
    private void addBuyButton() {
        ItemStack buyButton = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = buyButton.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(I18n.get("gui.item.buy"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(I18n.get("itemdetail.buy.lore"));
            meta.setLore(lore);

            buyButton.setItemMeta(meta);
        }

        inventory.setItem(49, buyButton);
    }

    private void addDecorativeBorder() {
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

        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();

        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get("gui.back"));

            List<String> backLore = new ArrayList<>();
            backLore.add("");
            backLore.add(I18n.get("gui.back.to.market"));
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
    
    private ItemStack createAmountDisplay(Player player) {
        ItemStack display = new ItemStack(Material.PAPER);
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "itemdetail.amount_title"));
            
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("§e1 " + I18n.get(player, "marketitems.unit"));
            lore.add("");
            lore.add(I18n.get(player, "itemdetail.total_price", plugin.getEconomyManager().format(marketItem.getPrice() / marketItem.getAmount())));
            meta.setLore(lore);
            
            display.setItemMeta(meta);
        }
        
        return display;
    }
    
    private ItemStack createMultiplierDisplay(Player player) {
        ItemStack display = new ItemStack(Material.COMPASS);
        ItemMeta meta = display.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(I18n.get(player, "itemdetail.multiplier_title"));

            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(String.format(I18n.get(player, "itemdetail.current_multiplier"), DEFAULT_MULTIPLIER));
            lore.add("");
            lore.add(I18n.get(player, "itemdetail.multiplier_desc"));
            lore.add("");
            lore.add(I18n.get(player, "itemdetail.multiplier_adjust"));
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

    private String formatTime(java.sql.Timestamp timestamp, Player player) {
        if (timestamp == null) {
            return I18n.get(player, "time.just_now");
        }
        long diff = System.currentTimeMillis() - timestamp.getTime();
        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return I18n.get(player, "time.days_ago", days);
        } else if (hours > 0) {
            return I18n.get(player, "time.hours_ago", hours);
        } else if (minutes > 0) {
            return I18n.get(player, "time.minutes_ago", minutes);
        } else {
            return I18n.get(player, "time.just_now");
        }
    }
    
    public void open(Player player) {
        // 初始化玩家的购买数量和基数
        playerPurchaseAmounts.put(player.getUniqueId(), 1);
        playerMultiplier.put(player.getUniqueId(), DEFAULT_MULTIPLIER);
        
        // 根据玩家语言更新界面文本
        updateLanguageForPlayer(player);
        
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
        });
    }
    
    private void updateLanguageForPlayer(Player player) {
        // 更新购买按钮
        ItemStack buyButton = inventory.getItem(49);
        if (buyButton != null) {
            ItemMeta meta = buyButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(I18n.get(player, "gui.item.buy"));
                buyButton.setItemMeta(meta);
                inventory.setItem(49, buyButton);
            }
        }
        
        // 更新返回按钮
        ItemStack backButton = inventory.getItem(45);
        if (backButton != null) {
            ItemMeta meta = backButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(I18n.get(player, "gui.back"));
                List<String> backLore = new ArrayList<>();
                backLore.add("");
                backLore.add(I18n.get(player, "gui.back.to.market"));
                meta.setLore(backLore);
                backButton.setItemMeta(meta);
                inventory.setItem(45, backButton);
            }
        }
        
        // 更新其他所有 localized 组件
        addInfoButtons(player);
        addQuantityControls(player);
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
                lore.add("§e" + amount + " " + I18n.get(player, "marketitems.unit"));
                lore.add("");
                double totalPrice = (marketItem.getPrice() / marketItem.getAmount()) * amount;
                lore.add(I18n.get(player, "itemdetail.total_price", plugin.getEconomyManager().format(totalPrice)));
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
                lore.add(String.format(I18n.get(player, "itemdetail.current_multiplier"), multiplier));
                lore.add("");
                lore.add(I18n.get(player, "itemdetail.multiplier_desc"));
                lore.add("");
                lore.add(I18n.get(player, "itemdetail.multiplier_adjust"));
                meta.setLore(lore);
                multiplierDisplay.setItemMeta(meta);
                inventory.setItem(40, multiplierDisplay);
            }
        }
        
        // 更新按钮的基数提示
        updateButtonLore(30, multiplier, player);
        updateButtonLore(32, multiplier, player);
        updateButtonLore(39, multiplier, player);
        updateButtonLore(41, multiplier, player);
        
        // 重新打开界面以更新显示
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.openInventory(inventory);
            // 清除更新标志
            playerUpdating.remove(player.getUniqueId());
        });
    }
    
    private void updateButtonLore(int slot, int multiplier, Player player) {
        ItemStack item = inventory.getItem(slot);
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 2) {
                lore.set(1, I18n.get(player, "itemdetail.current_multiplier", multiplier));
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