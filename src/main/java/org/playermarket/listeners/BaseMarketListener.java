package org.playermarket.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.gui.DelistingDetailGUI;
import org.playermarket.gui.ModifyBuyOrderGUI;
import org.playermarket.gui.ItemDetailGUI;
import org.playermarket.gui.MarketGUI;
import org.playermarket.gui.MarketItemsGUI;
import org.playermarket.gui.MyListingsGUI;
import org.playermarket.gui.WarehouseGUI;
import org.playermarket.gui.BuyOrderGUI;
import org.playermarket.gui.BuyOrderDetailGUI;
import org.playermarket.gui.MyBuyOrdersGUI;
import org.playermarket.gui.PlayerShopGUI;
import org.playermarket.gui.AllPlayerShopsGUI;
import org.playermarket.model.MarketItem;
import org.playermarket.model.WarehouseItem;
import org.playermarket.model.BuyOrder;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;
import org.playermarket.utils.I18n;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 市场监听器基类，包含共享字段和方法
 * 用于被具体的监听器类继承
 */
public abstract class BaseMarketListener {
    protected final PlayerMarket plugin;
    protected final MarketGUI marketGUI;
    protected final DatabaseManager dbManager;
    protected final EconomyManager economyManager;
    
    protected final Map<UUID, ItemDetailGUI> playerDetailGUIs = new HashMap<>();
    protected final Map<UUID, MyListingsGUI> playerMyListingsGUIs = new HashMap<>();
    protected final Map<UUID, DelistingDetailGUI> playerDelistingGUIs = new HashMap<>();
    protected final Map<UUID, WarehouseGUI> playerWarehouseGUIs = new HashMap<>();
    protected final Map<UUID, MarketItemsGUI> playerMarketItemsGUIs = new HashMap<>();
    protected final Map<UUID, BuyOrderGUI> playerBuyOrderGUIs = new HashMap<>();
    protected final Map<UUID, BuyOrderDetailGUI> playerBuyOrderDetailGUIs = new HashMap<>();
    protected final Map<UUID, MyBuyOrdersGUI> playerMyBuyOrdersGUIs = new HashMap<>();
    protected final Map<UUID, ModifyBuyOrderGUI> playerModifyBuyOrderGUIs = new HashMap<>();
    protected final Map<UUID, PlayerShopGUI> playerPlayerShopGUIs = new HashMap<>();
    protected final Map<UUID, AllPlayerShopsGUI> playerAllPlayerShopsGUIs = new HashMap<>();

    public BaseMarketListener(PlayerMarket plugin) {
        this.plugin = plugin;
        this.marketGUI = plugin.getMarketGUI();
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
    }

    /**
     * 检查是否是自定义GUI
     */
    protected boolean isCustomGUI(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        Object holder = inventory.getHolder();
        return holder instanceof MarketGUI ||
            holder instanceof MarketItemsGUI ||
            holder instanceof BuyOrderGUI ||
            holder instanceof BuyOrderDetailGUI ||
            holder instanceof MyBuyOrdersGUI ||
            holder instanceof ItemDetailGUI ||
            holder instanceof MyListingsGUI ||
            holder instanceof DelistingDetailGUI ||
            holder instanceof WarehouseGUI ||
            holder instanceof ModifyBuyOrderGUI ||
            holder instanceof PlayerShopGUI ||
            holder instanceof AllPlayerShopsGUI;
    }

    /**
     * 从物品Lore中提取市场物品ID
     */
    protected Integer extractMarketItemId(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }

        for (String line : lore) {
            if (line == null) {
                continue;
            }
            String plain = org.bukkit.ChatColor.stripColor(line).trim();
            String idStr = null;
            
            if (plain.startsWith("商品ID:")) {
                idStr = plain.substring("商品ID:".length()).trim();
            } else if (plain.startsWith("物品ID:")) {
                idStr = plain.substring("物品ID:".length()).trim();
            } else if (plain.startsWith("Item ID:")) {
                idStr = plain.substring("Item ID:".length()).trim();
            }
            
            if (idStr != null) {
                try {
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    protected Integer extractWarehouseItemId(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }

        for (String line : lore) {
            if (line == null) {
                continue;
            }
            String plain = org.bukkit.ChatColor.stripColor(line).trim();
            String idStr = null;

            if (plain.startsWith("仓库物品ID:")) {
                idStr = plain.substring("仓库物品ID:".length()).trim();
            } else if (plain.startsWith("Item ID:")) {
                idStr = plain.substring("Item ID:".length()).trim();
            }
            
            if (idStr != null) {
                try {
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return null;
    }

    /**
     * 从物品Lore中提取收购订单ID
     */
    protected Integer extractBuyOrderId(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }

        // 获取订单ID前缀，支持国际化
        String orderIdPrefix = getOrderIdPrefix();
        
        for (String line : lore) {
            if (line == null) {
                continue;
            }
            String plain = org.bukkit.ChatColor.stripColor(line).trim();
            String idStr = null;
            
            // 使用统一的I18n前缀进行匹配
            if (plain.startsWith(orderIdPrefix)) {
                idStr = plain.substring(orderIdPrefix.length()).trim();
            }
            
            if (idStr != null) {
                try {
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return null;
    }
    
    /**
     * 获取订单ID前缀，支持国际化
     * 从I18n获取统一的前缀，如果获取失败则使用默认值作为后备
     */
    protected String getOrderIdPrefix() {
        // 尝试从I18n获取统一前缀
        try {
            String prefix = I18n.get("order.id.prefix");
            if (prefix != null && !prefix.trim().isEmpty()) {
                // 确保前缀以冒号结尾，保持向后兼容
                String trimmed = prefix.trim();
                if (!trimmed.endsWith(":")) {
                    trimmed += ":";
                }
                return trimmed;
            }
        } catch (Exception e) {
            // I18n键可能不存在，使用默认值
        }
        
        // 默认后备值，支持中英文
        // 注意：这里使用默认语言环境的前缀，实际应通过玩家语言环境获取
        return "订单ID:";
    }

    /**
     * 打开我的上架物品界面
     */
    protected void openMyListingsGUI(Player player) {
        MyListingsGUI myListingsGUI = new MyListingsGUI(plugin, player);
        playerMyListingsGUIs.put(player.getUniqueId(), myListingsGUI);
        myListingsGUI.open();
    }
    
    /**
     * 打开下架详情界面
     */
    protected void openDelistingDetailGUI(Player player, MarketItem item) {
        DelistingDetailGUI delistingGUI = new DelistingDetailGUI(plugin, item, player);
        playerDelistingGUIs.put(player.getUniqueId(), delistingGUI);
        delistingGUI.open();
    }

    /**
     * 打开仓库界面
     */
    protected void openWarehouseGUI(Player player) {
        WarehouseGUI warehouseGUI = new WarehouseGUI(plugin, player);
        playerWarehouseGUIs.put(player.getUniqueId(), warehouseGUI);
        warehouseGUI.open();
    }
    
    /**
     * 打开市场物品界面
     */
    protected void openMarketItemsGUI(Player player) {
        MarketItemsGUI marketItemsGUI = new MarketItemsGUI(plugin, player);
        playerMarketItemsGUIs.put(player.getUniqueId(), marketItemsGUI);
        marketItemsGUI.open();
    }
    
    /**
     * 打开收购订单界面
     */
    protected void openBuyOrderGUI(Player player) {
        BuyOrderGUI buyOrderGUI = new BuyOrderGUI(plugin, player);
        playerBuyOrderGUIs.put(player.getUniqueId(), buyOrderGUI);
        buyOrderGUI.open();
    }

    /**
     * 打开我的收购订单界面
     */
    protected void openMyBuyOrdersGUI(Player player) {
        MyBuyOrdersGUI myBuyOrdersGUI = new MyBuyOrdersGUI(plugin, player);
        playerMyBuyOrdersGUIs.put(player.getUniqueId(), myBuyOrdersGUI);
        myBuyOrdersGUI.open();
    }

    /**
     * 打开修改收购订单界面
     */
    protected void openModifyBuyOrderGUI(Player player, BuyOrder order) {
        ModifyBuyOrderGUI modifyGUI = new ModifyBuyOrderGUI(plugin, order, player);
        playerModifyBuyOrderGUIs.put(player.getUniqueId(), modifyGUI);
        modifyGUI.open();
    }
    
    /**
     * 打开收购订单详情界面
     */
    protected void openBuyOrderDetailGUI(Player player, BuyOrder buyOrder) {
        BuyOrderDetailGUI buyOrderDetailGUI = new BuyOrderDetailGUI(plugin, buyOrder, player);
        playerBuyOrderDetailGUIs.put(player.getUniqueId(), buyOrderDetailGUI);
        buyOrderDetailGUI.open();
    }

    /**
     * 打开玩家商店界面
     */
    protected void openPlayerShopGUI(Player player) {
        PlayerShopGUI playerShopGUI = new PlayerShopGUI(plugin, player);
        playerPlayerShopGUIs.put(player.getUniqueId(), playerShopGUI);
        playerShopGUI.open();
    }

    /**
     * 打开全服店铺浏览界面
     */
    protected void openAllPlayerShopsGUI(Player player) {
        AllPlayerShopsGUI allPlayerShopsGUI = new AllPlayerShopsGUI(plugin, player);
        playerAllPlayerShopsGUIs.put(player.getUniqueId(), allPlayerShopsGUI);
        allPlayerShopsGUI.open();
    }

    /**
     * 计算玩家拥有的指定物品数量
     */
    protected int countPlayerItemAmount(Player player, ItemStack targetItem) {
        int count = 0;
        ItemStack compareItem = targetItem.clone();
        compareItem.setAmount(1);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(compareItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * 从玩家背包中移除指定数量的物品
     */
    protected boolean removeItemsFromInventory(Player player, ItemStack targetItem, int amount) {
        int remaining = amount;
        ItemStack compareItem = targetItem.clone();
        compareItem.setAmount(1);

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || !item.isSimilar(compareItem)) {
                continue;
            }

            if (item.getAmount() > remaining) {
                item.setAmount(item.getAmount() - remaining);
                player.getInventory().setItem(i, item);
                remaining = 0;
                break;
            } else {
                remaining -= item.getAmount();
                player.getInventory().setItem(i, null);
            }
        }

        player.updateInventory();
        return remaining <= 0;
    }

    /**
     * 获取物品的显示名称
     */
    protected String getItemDisplayName(ItemStack itemStack) {
        return I18n.getItemDisplayName(itemStack);
    }
    
    /**
     * 执行下架物品操作
     */
    protected void performDelistItem(Player player, MarketItem item, int delistAmount) {
        if (dbManager.partialDelistItem(item.getId(), player.getUniqueId(), delistAmount)) {
            ItemStack itemStack = item.getItemStack().clone();
            int maxStackSize = itemStack.getMaxStackSize();
            
            // 计算需要分成多少组
            int fullStacks = delistAmount / maxStackSize;
            int remainder = delistAmount % maxStackSize;
            
            int successCount = 0;
            
            // 添加完整的堆叠组
            for (int i = 0; i < fullStacks; i++) {
                ItemStack stackItem = itemStack.clone();
                stackItem.setAmount(maxStackSize);
                
                WarehouseItem warehouseItem = new WarehouseItem(
                    player.getUniqueId(),
                    player.getName(),
                    stackItem,
                    item.getUnitPrice() * maxStackSize,
                    item.getUnitPrice(),
                    item.getId()
                );
                
                if (dbManager.addWarehouseItem(warehouseItem)) {
                    successCount++;
                }
            }
            
            // 添加剩余部分
            if (remainder > 0) {
                ItemStack remainderItem = itemStack.clone();
                remainderItem.setAmount(remainder);
                
                WarehouseItem warehouseItem = new WarehouseItem(
                    player.getUniqueId(),
                    player.getName(),
                    remainderItem,
                    item.getUnitPrice() * remainder,
                    item.getUnitPrice(),
                    item.getId()
                );
                
                if (dbManager.addWarehouseItem(warehouseItem)) {
                    successCount++;
                }
            }
            
            int totalGroups = fullStacks + (remainder > 0 ? 1 : 0);
            
            if (successCount == totalGroups) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                player.sendMessage(I18n.get(player, "marketlistener.delist_success"));
                player.sendMessage(I18n.get(player, "marketlistener.delist_item", getItemDisplayName(itemStack) + " x" + delistAmount));
                player.sendMessage(I18n.get(player, "marketlistener.delist_groups", totalGroups));
                
                DelistingDetailGUI delistingGUI = playerDelistingGUIs.remove(player.getUniqueId());
                if (delistingGUI != null) {
                    delistingGUI.cleanup();
                }
                
                MyListingsGUI myListingsGUI = playerMyListingsGUIs.get(player.getUniqueId());
                if (myListingsGUI != null) {
                    myListingsGUI.refresh();
                    myListingsGUI.open();
                } else {
                    openMyListingsGUI(player);
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                player.sendMessage(I18n.get(player, "marketlistener.delist_partial", successCount, totalGroups));
            }
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "marketlistener.delist_failed"));
        }
    }
    
    /**
     * 从仓库提取物品
     */
    protected void withdrawWarehouseItem(Player player, WarehouseItem item, int clickedSlot) {
        ItemStack sample = item.getItemStack();
        int totalAmount = item.getAmount();
        int maxStackSize = Math.min(sample.getMaxStackSize(), 99);

        int canFit = countCanFit(player, sample, maxStackSize);
        if (canFit <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "marketlistener.inventory_full"));
            return;
        }

        int takeAmount = Math.min(totalAmount, canFit);

        int remaining = takeAmount;
        List<ItemStack> toGive = new ArrayList<>();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStackSize);
            ItemStack stack = sample.clone();
            stack.setAmount(give);
            toGive.add(stack);
            remaining -= give;
        }

        Map<Integer, ItemStack> leftover = new HashMap<>();
        for (ItemStack stack : toGive) {
            leftover.putAll(player.getInventory().addItem(stack));
        }

        int notGiven = 0;
        for (ItemStack l : leftover.values()) {
            if (l != null) {
                notGiven += l.getAmount();
            }
        }
        int givenAmount = takeAmount - notGiven;

        if (givenAmount <= 0) {
            for (ItemStack stack : toGive) {
                player.getInventory().removeItem(stack);
            }
            player.updateInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_insufficient"));
            return;
        }

        boolean ok;
        if (givenAmount >= totalAmount) {
            ok = dbManager.withdrawWarehouseItem(item.getId(), player.getUniqueId());
        } else {
            ok = dbManager.partialWithdrawWarehouseItem(item.getId(), player.getUniqueId(), givenAmount);
        }

        if (!ok) {
            for (ItemStack stack : toGive) {
                player.getInventory().removeItem(stack);
            }
            player.updateInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_failed"));
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        if (givenAmount >= totalAmount) {
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_success"));
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_item", getItemDisplayName(sample) + " x" + totalAmount));
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top != null) {
                top.setItem(clickedSlot, new ItemStack(org.bukkit.Material.AIR));
            }
        } else {
            int left = totalAmount - givenAmount;
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_partial"));
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_partial_item", getItemDisplayName(sample) + " x" + givenAmount));
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_remaining", left));
            player.sendMessage(I18n.get(player, "marketlistener.withdraw_partial_tip"));

            Inventory top = player.getOpenInventory().getTopInventory();
            if (top != null) {
                ItemStack display = top.getItem(clickedSlot);
                if (display != null && !display.getType().isAir()) {
                    ItemMeta meta = display.getItemMeta();
                    if (meta != null) {
                        List<String> lore = meta.getLore();
                        if (lore != null) {
                            for (int i = 0; i < lore.size(); i++) {
                                String plain = org.bukkit.ChatColor.stripColor(lore.get(i)).trim();
                                if (plain.startsWith("数量:")) {
                                    lore.set(i, "§7数量: §e" + left);
                                    break;
                                }
                            }
                            meta.setLore(lore);
                            display.setItemMeta(meta);
                            top.setItem(clickedSlot, display);
                        }
                    }
                }
            }
        }

        player.updateInventory();
    }

    /**
     * 计算玩家背包可以容纳多少指定物品
     */
    protected int countCanFit(Player player, ItemStack sample, int maxStackSize) {
        int canFit = 0;
        ItemStack compare = sample.clone();
        compare.setAmount(1);

        ItemStack[] contents = player.getInventory().getStorageContents();
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType().isAir()) {
                canFit += maxStackSize;
                continue;
            }
            if (stack.isSimilar(compare)) {
                int space = maxStackSize - stack.getAmount();
                if (space > 0) {
                    canFit += space;
                }
            }
        }
        return canFit;
    }
}
