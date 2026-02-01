package org.playermarket.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
import org.playermarket.model.MarketItem;
import org.playermarket.model.WarehouseItem;
import org.playermarket.model.BuyOrder;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MarketListener implements Listener {
    private boolean isCustomGUI(Inventory inventory) {
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
            holder instanceof ModifyBuyOrderGUI;
    }
    private final PlayerMarket plugin;
    private final MarketGUI marketGUI;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    
    private final Map<UUID, ItemDetailGUI> playerDetailGUIs = new HashMap<>();
    private final Map<UUID, MyListingsGUI> playerMyListingsGUIs = new HashMap<>();
    private final Map<UUID, DelistingDetailGUI> playerDelistingGUIs = new HashMap<>();
    private final Map<UUID, WarehouseGUI> playerWarehouseGUIs = new HashMap<>();
    private final Map<UUID, MarketItemsGUI> playerMarketItemsGUIs = new HashMap<>();
    private final Map<UUID, BuyOrderGUI> playerBuyOrderGUIs = new HashMap<>();
    private final Map<UUID, BuyOrderDetailGUI> playerBuyOrderDetailGUIs = new HashMap<>();
    private final Map<UUID, MyBuyOrdersGUI> playerMyBuyOrdersGUIs = new HashMap<>();
    private final Map<UUID, ModifyBuyOrderGUI> playerModifyBuyOrderGUIs = new HashMap<>();

    private Integer extractMarketItemId(ItemStack itemStack) {
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
            if (plain.startsWith("商品ID:")) {
                String idStr = plain.substring("商品ID:".length()).trim();
                try {
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        return null;
    }

    private Integer extractWarehouseItemId(ItemStack itemStack) {
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
            if (plain.startsWith("仓库物品ID:")) {
                String idStr = plain.substring("仓库物品ID:".length()).trim();
                try {
                    return Integer.parseInt(idStr);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        return null;
    }

    public MarketListener(PlayerMarket plugin) {
        this.plugin = plugin;
        this.marketGUI = plugin.getMarketGUI();
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        
        if (isCustomGUI(inventory)) {
            event.setCancelled(true);
        }
        
        if (inventory.getHolder() instanceof MarketGUI) {
            
            int slot = event.getSlot();
            
            if (slot == 10) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMarketItemsGUI(player);
                return;
            } else if (slot == 12) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openBuyOrderGUI(player);
                return;
            } else if (slot == 14) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMyListingsGUI(player);
                return;
            } else if (slot == 16) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMyBuyOrdersGUI(player);
                return;
            } else if (slot == 22) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openWarehouseGUI(player);
                return;
            } else if (slot == 4) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return;
            }
        }
        
        if (inventory.getHolder() instanceof MarketItemsGUI) {
            
            MarketItemsGUI marketItemsGUI = (MarketItemsGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketItemsGUI.setCurrentPage(marketItemsGUI.getCurrentPage() - 1);
                marketItemsGUI.refresh();
                return;
            } else if (slot == 46) {
                // 返回按钮：返回上一页（第一页则返回主菜单）
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                if (marketItemsGUI.getCurrentPage() > 1) {
                    marketItemsGUI.setCurrentPage(marketItemsGUI.getCurrentPage() - 1);
                    marketItemsGUI.refresh();
                } else {
                    marketGUI.openMarketGUI(player);
                }
                return;
            } else if (slot == 53) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketItemsGUI.setCurrentPage(marketItemsGUI.getCurrentPage() + 1);
                marketItemsGUI.refresh();
                return;
            } else if (slot == 49) {
                // 刷新页面
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketItemsGUI.refresh();
                return;
            }
            
            if (slot >= 0 && slot < 45) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }
                
                // 获取点击的商品
                List<MarketItem> items = marketItemsGUI.getMarketItems();
                int index = (marketItemsGUI.getCurrentPage() - 1) * 45 + slot;
                if (index >= 0 && index < items.size()) {
                    MarketItem item = items.get(index);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                    ItemDetailGUI detailGUI = new ItemDetailGUI(plugin, item);
                    playerDetailGUIs.put(player.getUniqueId(), detailGUI);
                    detailGUI.open(player);
                }
            }
        }
        
        if (inventory.getHolder() instanceof BuyOrderGUI) {
            
            BuyOrderGUI buyOrderGUI = (BuyOrderGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                buyOrderGUI.setCurrentPage(buyOrderGUI.getCurrentPage() - 1);
                buyOrderGUI.refresh();
                return;
            } else if (slot == 46) {
                // 返回按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                BuyOrderGUI stored = playerBuyOrderGUIs.remove(player.getUniqueId());
                if (stored != null) {
                    stored.cleanup();
                }
                marketGUI.openMarketGUI(player);
                return;
            } else if (slot == 53) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                buyOrderGUI.setCurrentPage(buyOrderGUI.getCurrentPage() + 1);
                buyOrderGUI.refresh();
                return;
            } else if (slot == 49) {
                // 刷新
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                buyOrderGUI.refresh();
                return;
            }
            
            if (slot >= 0 && slot < 45) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }
                
                // 获取点击的收购订单
                List<BuyOrder> orders = buyOrderGUI.getBuyOrders();
                int index = (buyOrderGUI.getCurrentPage() - 1) * 28 + slot;
                if (index >= 0 && index < orders.size()) {
                    BuyOrder order = orders.get(index);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

                    // 查看详情 / 出售
                    BuyOrderDetailGUI detailGUI = new BuyOrderDetailGUI(plugin, order, player);
                    playerBuyOrderDetailGUIs.put(player.getUniqueId(), detailGUI);
                    detailGUI.open();
                }
            }
        }
        
        if (inventory.getHolder() instanceof BuyOrderDetailGUI) {

            BuyOrderDetailGUI detailGUI = (BuyOrderDetailGUI) inventory.getHolder();
            int slot = event.getSlot();

            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                BuyOrderDetailGUI stored = playerBuyOrderDetailGUIs.remove(player.getUniqueId());
                if (stored != null) {
                    stored.cleanup(player);
                }
                BuyOrderGUI buyOrderGUI = playerBuyOrderGUIs.get(player.getUniqueId());
                if (buyOrderGUI != null) {
                    buyOrderGUI.refresh();
                    buyOrderGUI.open();
                } else {
                    openBuyOrderGUI(player);
                }
                return;
            }

            if (slot == 30) {
                // 增加数量按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                detailGUI.increaseAmount();
                return;
            } else if (slot == 32) {
                // 减少数量按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                detailGUI.decreaseAmount();
                return;
            } else if (slot == 39) {
                // 增加基数按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                detailGUI.increaseMultiplier();
                return;
            } else if (slot == 40) {
                // 基数显示
                return;
            } else if (slot == 31) {
                // 数量显示
                return;
            }

            if (slot == 41) {
                // 减少基数按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                detailGUI.decreaseMultiplier();
                return;
            }

            if (slot == 49) {
                BuyOrder order = detailGUI.getBuyOrder();
                if (order == null || order.isFulfilled() || order.getRemainingAmount() <= 0) {
                    player.sendMessage("§c该收购订单已完成或不存在！");
                    detailGUI.refresh();
                    return;
                }

                int sellAmount = BuyOrderDetailGUI.getPlayerSellAmounts().getOrDefault(player.getUniqueId(), 1);
                int max = Math.min(order.getRemainingAmount(), countPlayerItemAmount(player, order.getItemStack()));
                if (max <= 0) {
                    player.sendMessage("§c你的物品数量不足，无法出售！");
                    return;
                }

                sellAmount = Math.min(sellAmount, max);
                if (sellAmount <= 0) {
                    player.sendMessage("§c出售数量无效！");
                    return;
                }

                // 检查买家余额
                double requiredAmount = sellAmount * order.getUnitPrice();
                double buyerBalance = economyManager.getBalance(order.getBuyerUuid());
                if (buyerBalance < requiredAmount) {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                    player.sendMessage("§c收购方余额不足，无法完成交易！");
                    player.sendMessage("§7需要: §e" + economyManager.format(requiredAmount));
                    player.sendMessage("§7买家余额: §e" + economyManager.format(buyerBalance));
                    return;
                }

                ItemStack targetItem = order.getItemStack();
                boolean removed = removeItemsFromInventory(player, targetItem, sellAmount);
                if (!removed) {
                    player.sendMessage("§c扣除物品失败，请稍后重试！");
                    return;
                }

                boolean ok = dbManager.sellToBuyOrder(order.getId(), player.getUniqueId(), player.getName(), sellAmount);
                if (!ok) {
                    ItemStack rollback = targetItem.clone();
                    rollback.setAmount(sellAmount);
                    player.getInventory().addItem(rollback);
                    player.updateInventory();
                    player.sendMessage("§c出售失败，订单可能已被他人更新！");
                    detailGUI.refresh();
                    return;
                }

                double income = sellAmount * order.getUnitPrice();
                
                // 扣除收购方的钱
                boolean withdrawn = economyManager.withdraw(order.getBuyerUuid(), income);
                if (!withdrawn) {
                    ItemStack rollback = targetItem.clone();
                    rollback.setAmount(sellAmount);
                    player.getInventory().addItem(rollback);
                    player.updateInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                    player.sendMessage("§c收购方余额不足，交易失败！");
                    detailGUI.refresh();
                    return;
                }
                
                economyManager.deposit(player, income);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

                final String itemName = getItemDisplayName(targetItem);
                final int soldAmount = sellAmount;
                final double soldIncome = income;
                final String sellerName = player.getName();
                final UUID buyerUuid = order.getBuyerUuid();
                final String buyerName = order.getBuyerName();

                player.sendMessage("§a=== 出售成功 ===");
                player.sendMessage("§7物品: §e" + itemName + " x" + soldAmount);
                player.sendMessage("§7收入: §e" + economyManager.format(soldIncome));

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    String notificationMessage = "§a收购订单成交 §7| §e" + itemName + " x" + soldAmount +
                            " §7| §e支付: " + economyManager.format(soldIncome) + " §7| 卖家: " + sellerName;
                    String notificationData = itemName + "," + soldAmount + "," + soldIncome + "," + sellerName;
                    dbManager.addNotification(buyerUuid, buyerName, "buyorder", notificationMessage, notificationData);

                    Player buyer = plugin.getServer().getPlayer(buyerUuid);
                    if (buyer != null && buyer.isOnline()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            buyer.playSound(buyer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                            buyer.sendMessage("§a=== 收购订单成交 ===");
                            buyer.sendMessage("§7物品: §e" + itemName + " x" + soldAmount);
                            buyer.sendMessage("§7卖家: §e" + sellerName);
                            buyer.sendMessage("§7支出: §e" + economyManager.format(soldIncome));
                        });
                    }
                });

                detailGUI.refresh();
                BuyOrderGUI buyOrderGUI = playerBuyOrderGUIs.get(player.getUniqueId());
                if (buyOrderGUI != null) {
                    buyOrderGUI.refresh();
                }
                MyBuyOrdersGUI myBuyOrdersGUI = playerMyBuyOrdersGUIs.get(player.getUniqueId());
                if (myBuyOrdersGUI != null) {
                    myBuyOrdersGUI.refresh();
                }
                return;
            }
        }

        if (inventory.getHolder() instanceof MyBuyOrdersGUI) {

            MyBuyOrdersGUI myBuyOrdersGUI = (MyBuyOrdersGUI) inventory.getHolder();
            int slot = event.getSlot();

            if (slot == 45) {
                // 上一页
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myBuyOrdersGUI.setCurrentPage(myBuyOrdersGUI.getCurrentPage() - 1);
                myBuyOrdersGUI.refresh();
                return;
            } else if (slot == 46) {
                // 返回按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                MyBuyOrdersGUI stored = playerMyBuyOrdersGUIs.remove(player.getUniqueId());
                if (stored != null) {
                    stored.cleanup();
                }
                marketGUI.openMarketGUI(player);
                return;
            } else if (slot == 47) {
                // 刷新按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myBuyOrdersGUI.refresh();
                return;
            } else if (slot == 53) {
                // 下一页
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myBuyOrdersGUI.setCurrentPage(myBuyOrdersGUI.getCurrentPage() + 1);
                myBuyOrdersGUI.refresh();
                return;
            } else if (slot == 49) {
                // 点击命名牌刷新
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myBuyOrdersGUI.refresh();
                return;
            }

            if (slot >= 0 && slot < 45) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }

                // 获取点击的收购订单
                List<BuyOrder> orders = myBuyOrdersGUI.getBuyOrders();
                int index = (myBuyOrdersGUI.getCurrentPage() - 1) * 28 + slot;
                if (index >= 0 && index < orders.size()) {
                    BuyOrder order = orders.get(index);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

                    // 重新从数据库获取最新订单状态
                    BuyOrder latestOrder = dbManager.getBuyOrderById(order.getId());
                    if (latestOrder == null) {
                        player.sendMessage("§c订单不存在！");
                        myBuyOrdersGUI.refresh();
                        return;
                    }
                    
                    plugin.getLogger().info("订单状态检查 - ID: " + latestOrder.getId() + ", fulfilled: " + latestOrder.isFulfilled() + ", amount: " + latestOrder.getAmount() + ", remaining: " + latestOrder.getRemainingAmount());
                    
                    int acquiredAmount = latestOrder.getAmount() - latestOrder.getRemainingAmount();
                    boolean isPartiallyFulfilled = acquiredAmount > 0 && !latestOrder.isFulfilled();
                    
                    if (event.getClick().isRightClick()) {
                        if (!latestOrder.isFulfilled()) {
                            boolean cancelled = dbManager.cancelBuyOrder(latestOrder.getId(), player.getUniqueId());
                            if (cancelled) {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                player.sendMessage("§a收购订单已取消！");
                                myBuyOrdersGUI.refresh();
                            } else {
                                player.sendMessage("§c取消收购订单失败！");
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                            player.sendMessage("§c已完成的订单无法取消！");
                        }
                    } else if (event.getClick().isLeftClick()) {
                        if (latestOrder.isFulfilled()) {
                            // 已完成状态：转移所有物品到仓库
                            if (acquiredAmount > 0) {
                                ItemStack itemStack = latestOrder.getItemStack();
                                itemStack.setAmount(acquiredAmount);

                                WarehouseItem warehouseItem = new WarehouseItem(
                                    player.getUniqueId(),
                                    player.getName(),
                                    itemStack,
                                    latestOrder.getUnitPrice() * acquiredAmount,
                                    latestOrder.getUnitPrice(),
                                    latestOrder.getId()
                                );

                                boolean added = dbManager.addWarehouseItem(warehouseItem);
                                if (added) {
                                    dbManager.deleteCompletedBuyOrder(latestOrder.getId(), player.getUniqueId());
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                    player.sendMessage("§a收购的物品已转移到仓库！");
                                    myBuyOrdersGUI.refresh();
                                } else {
                                    player.sendMessage("§c物品转移到仓库失败！");
                                }
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                player.sendMessage("§c该订单没有收购到任何物品！");
                            }
                        } else if (isPartiallyFulfilled) {
                            // 部分完成状态：转移已收购的物品到仓库（提货后要把订单的amount同步为remaining_amount，避免重复提货）
                            if (acquiredAmount > 0) {
                                boolean claimed = dbManager.claimBuyOrderAcquiredItems(latestOrder.getId(), player.getUniqueId(), acquiredAmount);
                                if (!claimed) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                    player.sendMessage("§c没有可提取的物品（可能已提取或订单状态已变化）！");
                                    myBuyOrdersGUI.refresh();
                                    return;
                                }

                                ItemStack itemStack = latestOrder.getItemStack();
                                itemStack.setAmount(acquiredAmount);

                                WarehouseItem warehouseItem = new WarehouseItem(
                                    player.getUniqueId(),
                                    player.getName(),
                                    itemStack,
                                    latestOrder.getUnitPrice() * acquiredAmount,
                                    latestOrder.getUnitPrice(),
                                    latestOrder.getId()
                                );

                                boolean added = dbManager.addWarehouseItem(warehouseItem);
                                if (added) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                    player.sendMessage("§a已收购的物品已转移到仓库！");
                                    myBuyOrdersGUI.refresh();
                                } else {
                                    player.sendMessage("§c物品转移到仓库失败！");
                                    myBuyOrdersGUI.refresh();
                                }
                            } else {
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                player.sendMessage("§c该订单没有收购到任何物品！");
                            }
                        } else {
                            // 未完成状态：修改收购数量
                            openModifyBuyOrderGUI(player, latestOrder);
                        }
                    }
                }
            }
        }

        if (inventory.getHolder() instanceof ItemDetailGUI) {
            
            ItemDetailGUI detailGUI = (ItemDetailGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                MarketItemsGUI marketItemsGUI = playerMarketItemsGUIs.get(player.getUniqueId());
                if (marketItemsGUI != null) {
                    marketItemsGUI.open();
                } else {
                    openMarketItemsGUI(player);
                }
                return;
            }
            
            if (slot == 30) {
                detailGUI.increaseAmount(player);
            } else if (slot == 32) {
                detailGUI.decreaseAmount(player);
            } else if (slot == 39) {
                detailGUI.increaseMultiplier(player);
            } else if (slot == 41) {
                detailGUI.decreaseMultiplier(player);
            } else if (slot == 49) {
                int purchaseAmount = detailGUI.getPurchaseAmount(player);
                purchaseItem(player, detailGUI.getMarketItem(), purchaseAmount);
            }
        }
        
        if (inventory.getHolder() instanceof MyListingsGUI) {
            
            MyListingsGUI myListingsGUI = (MyListingsGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myListingsGUI.previousPage();
                return;
            } else if (slot == 46) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketGUI.openMarketGUI(player);
                return;
            } else if (slot == 40) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myListingsGUI.refresh();
                return;
            } else if (slot == 48) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketGUI.openMarketGUI(player, marketGUI.getCurrentPage(player));
                return;
            } else if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myListingsGUI.refresh();
                return;
            } else if (slot == 53) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                myListingsGUI.nextPage();
                return;
            }
            
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 0 && rawSlot < 45) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }

                Integer itemId = extractMarketItemId(clickedItem);
                if (itemId == null) {
                    player.sendMessage("§c商品不存在！");
                    return;
                }

                MarketItem item = dbManager.getItemById(itemId);
                if (item == null || item.isSold() || !player.getUniqueId().equals(item.getSellerUuid())) {
                    player.sendMessage("§c商品不存在！");
                    return;
                }

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openDelistingDetailGUI(player, item);
            }
        }
        
        if (inventory.getHolder() instanceof DelistingDetailGUI) {
            
            DelistingDetailGUI delistingGUI = (DelistingDetailGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                DelistingDetailGUI storedGUI = playerDelistingGUIs.remove(player.getUniqueId());
                if (storedGUI != null) {
                    storedGUI.cleanup();
                }
                MyListingsGUI myListingsGUI = playerMyListingsGUIs.get(player.getUniqueId());
                if (myListingsGUI != null) {
                    myListingsGUI.refresh();
                    myListingsGUI.open();
                } else {
                    openMyListingsGUI(player);
                }
                return;
            } else if (slot == 30) {
                delistingGUI.increaseAmount();
                return;
            } else if (slot == 32) {
                delistingGUI.decreaseAmount();
                return;
            } else if (slot == 39) {
                delistingGUI.increaseMultiplier();
                return;
           } else if (slot == 41) {
                delistingGUI.decreaseMultiplier();
            } else if (slot == 48) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                performDelistItem(player, delistingGUI.getMarketItem(), delistingGUI.getDelistAmount());
                return;
            } else if (slot == 50) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                performDelistItem(player, delistingGUI.getMarketItem(), delistingGUI.getMarketItem().getAmount());
                return;
            }
        }
        
        if (inventory.getHolder() instanceof WarehouseGUI) {
            
            WarehouseGUI warehouseGUI = (WarehouseGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 45) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                warehouseGUI.previousPage();
                return;
            } else if (slot == 48) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                marketGUI.openMarketGUI(player);
                return;
            } else if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                warehouseGUI.refresh();
                return;
            } else if (slot == 53) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                warehouseGUI.nextPage();
                return;
            }
            
            if (slot >= 0 && slot < 45) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType().isAir()) {
                    return;
                }

                Integer itemId = extractWarehouseItemId(clickedItem);
                if (itemId == null) {
                    player.sendMessage("§c物品不存在！");
                    return;
                }

                WarehouseItem item = dbManager.getWarehouseItemById(itemId);
                if (item == null || !player.getUniqueId().equals(item.getOwnerUuid())) {
                    player.sendMessage("§c物品不存在！");
                    return;
                }

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                withdrawWarehouseItem(player, item, slot);
            }
        }
        
        if (inventory.getHolder() instanceof ModifyBuyOrderGUI) {
            
            ModifyBuyOrderGUI modifyGUI = (ModifyBuyOrderGUI) inventory.getHolder();
            int slot = event.getSlot();
            
            if (slot == 30) {
                // 增加数量按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                modifyGUI.increaseAmount();
                return;
            } else if (slot == 32) {
                // 减少数量按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                modifyGUI.decreaseAmount();
                return;
            } else if (slot == 39) {
                // 增加倍数按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                modifyGUI.increaseMultiplier();
                return;
            } else if (slot == 41) {
                // 减少倍数按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                modifyGUI.decreaseMultiplier();
                return;
            } else if (slot == 48) {
                // 返回按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                ModifyBuyOrderGUI storedGUI = playerModifyBuyOrderGUIs.remove(player.getUniqueId());
                if (storedGUI != null) {
                    storedGUI.cleanup(player);
                }
                
                MyBuyOrdersGUI myBuyOrdersGUI = playerMyBuyOrdersGUIs.get(player.getUniqueId());
                if (myBuyOrdersGUI != null) {
                    myBuyOrdersGUI.open();
                } else {
                    openMyBuyOrdersGUI(player);
                }
                return;
            } else if (slot == 49) {
                // 确认修改按钮
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                int newAmount = modifyGUI.getNewAmount();
                BuyOrder buyOrder = modifyGUI.getBuyOrder();
                
                boolean updated = dbManager.updateBuyOrderAmount(buyOrder.getId(), player.getUniqueId(), newAmount);
                if (updated) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                    player.sendMessage("§a收购数量已更新！");
                    
                    // 关闭修改界面，返回我的收购界面
                    ModifyBuyOrderGUI storedGUI = playerModifyBuyOrderGUIs.remove(player.getUniqueId());
                    if (storedGUI != null) {
                        storedGUI.cleanup(player);
                    }
                    
                    MyBuyOrdersGUI myBuyOrdersGUI = playerMyBuyOrdersGUIs.get(player.getUniqueId());
                    if (myBuyOrdersGUI != null) {
                        myBuyOrdersGUI.refresh();
                        myBuyOrdersGUI.open();
                    } else {
                        openMyBuyOrdersGUI(player);
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                    player.sendMessage("§c更新失败！订单可能已被他人修改");
                }
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (isCustomGUI(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (isCustomGUI(event.getInventory())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Inventory top = player.getOpenInventory().getTopInventory();
        if (isCustomGUI(top)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MarketGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                marketGUI.removePlayerInventory(player);
            }
        }
        
        if (event.getInventory().getHolder() instanceof ItemDetailGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                ItemDetailGUI detailGUI = playerDetailGUIs.remove(player.getUniqueId());
                if (detailGUI != null) {
                    detailGUI.cleanup(player);
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof MyListingsGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                MyListingsGUI myListingsGUI = playerMyListingsGUIs.remove(player.getUniqueId());
                if (myListingsGUI != null) {
                    myListingsGUI.cleanup();
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof DelistingDetailGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                DelistingDetailGUI delistingGUI = playerDelistingGUIs.remove(player.getUniqueId());
                if (delistingGUI != null) {
                    delistingGUI.cleanup();
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof WarehouseGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                WarehouseGUI warehouseGUI = playerWarehouseGUIs.remove(player.getUniqueId());
                if (warehouseGUI != null) {
                    warehouseGUI.cleanup();
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof ModifyBuyOrderGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                ModifyBuyOrderGUI modifyGUI = playerModifyBuyOrderGUIs.remove(player.getUniqueId());
                if (modifyGUI != null) {
                    modifyGUI.cleanup(player);
                }
            }
        }
    }
    
    private void purchaseItem(Player player, MarketItem item, int purchaseAmount) {
        if (item == null) {
            player.sendMessage("§c商品不存在！");
            return;
        }
        
        // 检查商品是否已被购买
        if (item.isSold()) {
            player.sendMessage("§c该商品已被购买！");
            return;
        }
        
        // 检查购买数量是否有效
        if (purchaseAmount <= 0 || purchaseAmount > item.getAmount()) {
            player.sendMessage("§c购买数量无效！");
            return;
        }
        
        // 计算单价和总价
        double unitPrice = item.getPrice() / item.getAmount();
        double totalPrice = unitPrice * purchaseAmount;
        
        // 检查玩家余额
        double balance = economyManager.getBalance(player);
        
        if (balance < totalPrice) {
            player.sendMessage("§c余额不足！");
            player.sendMessage("§7需要: §e" + economyManager.format(totalPrice));
            player.sendMessage("§7拥有: §e" + economyManager.format(balance));
            return;
        }
        
        ItemStack itemStack = item.getItemStack().clone();
        itemStack.setAmount(purchaseAmount);
        
        // 执行购买
        if (dbManager.purchaseItem(item.getId(), player.getUniqueId(), player.getName(), purchaseAmount)) {
            // 播放成功音效
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            
            // 扣款
            economyManager.withdraw(player, totalPrice);
            
            // 获取物品名称（优先使用自定义名称）
            final String itemName;
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                itemName = itemStack.getItemMeta().getDisplayName();
            } else {
                String tempName = itemStack.getI18NDisplayName();
                if (tempName == null) {
                    itemName = itemStack.getType().name();
                } else {
                    itemName = tempName;
                }
            }
            
            // 给卖家加钱（无论卖家是否在线）
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Player seller = plugin.getServer().getPlayer(item.getSellerUuid());
                
                // 无论卖家是否在线，都给卖家加钱
                economyManager.deposit(item.getSellerUuid(), totalPrice);
                
                // 添加通知到数据库
                String notificationMessage = "§a商品已售出 §7| §e" + itemName + " x" + purchaseAmount + " §7| §e" + economyManager.format(totalPrice);
                String notificationData = itemName + "," + purchaseAmount + "," + totalPrice;
                dbManager.addNotification(item.getSellerUuid(), item.getSellerName(), "sale", notificationMessage, notificationData);
                
                // 如果卖家在线，发送通知消息和播放音效
                if (seller != null && seller.isOnline()) {
                    seller.playSound(seller.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    seller.sendMessage("§a=== 商品已售出 ===");
                    seller.sendMessage("§7买家: §e" + player.getName());
                    seller.sendMessage("§7数量: §e" + purchaseAmount + " 个");
                    seller.sendMessage("§7收入: §e" + economyManager.format(totalPrice));
                }
            });
            
            // 购买的物品转移到仓库（支持合并）
            org.playermarket.model.WarehouseItem warehouseItem = new org.playermarket.model.WarehouseItem(
                    player.getUniqueId(),
                    player.getName(),
                    itemStack,
                    totalPrice,
                    unitPrice,
                    item.getId()
            );

            boolean added = dbManager.addWarehouseItem(warehouseItem);
            if (!added) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                player.sendMessage("§c购买成功，但转移到仓库失败！请联系管理员");
            }

            // 发送成功消息
            player.sendMessage("§a=== 购买成功 ===");
            player.sendMessage("§7商品: §e" + itemName + " x" + purchaseAmount);
            player.sendMessage("§7单价: §e" + economyManager.format(unitPrice) + " / 个");
            player.sendMessage("§7总价: §e" + economyManager.format(totalPrice));
            player.sendMessage("§7已转移到仓库");
            
            // 返回上一页（购买市场）
            ItemDetailGUI detailGUI = playerDetailGUIs.remove(player.getUniqueId());
            if (detailGUI != null) {
                detailGUI.cleanup(player);
            }

            MarketItemsGUI marketItemsGUI = playerMarketItemsGUIs.get(player.getUniqueId());
            if (marketItemsGUI != null) {
                marketItemsGUI.refresh();
                marketItemsGUI.open();
            } else {
                openMarketItemsGUI(player);
            }
            
            plugin.getLogger().info(player.getName() + " 购买了商品: " + itemStack.getType().name() + " x" + purchaseAmount);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage("§c购买失败！商品可能已被他人购买");
        }
    }
    
    private void openMyListingsGUI(Player player) {
        MyListingsGUI myListingsGUI = new MyListingsGUI(plugin, player);
        playerMyListingsGUIs.put(player.getUniqueId(), myListingsGUI);
        myListingsGUI.open();
    }
    
    private void openDelistingDetailGUI(Player player, MarketItem item) {
        DelistingDetailGUI delistingGUI = new DelistingDetailGUI(plugin, item, player);
        playerDelistingGUIs.put(player.getUniqueId(), delistingGUI);
        delistingGUI.open();
    }
    
    private void openWarehouseGUI(Player player) {
        WarehouseGUI warehouseGUI = new WarehouseGUI(plugin, player);
        playerWarehouseGUIs.put(player.getUniqueId(), warehouseGUI);
        warehouseGUI.open();
    }
    
    private void openMarketItemsGUI(Player player) {
        MarketItemsGUI marketItemsGUI = new MarketItemsGUI(plugin, player);
        playerMarketItemsGUIs.put(player.getUniqueId(), marketItemsGUI);
        marketItemsGUI.open();
    }
    
    private void openBuyOrderGUI(Player player) {
        BuyOrderGUI buyOrderGUI = new BuyOrderGUI(plugin, player);
        playerBuyOrderGUIs.put(player.getUniqueId(), buyOrderGUI);
        buyOrderGUI.open();
    }

    private void openMyBuyOrdersGUI(Player player) {
        MyBuyOrdersGUI myBuyOrdersGUI = new MyBuyOrdersGUI(plugin, player);
        playerMyBuyOrdersGUIs.put(player.getUniqueId(), myBuyOrdersGUI);
        myBuyOrdersGUI.open();
    }

    private void openModifyBuyOrderGUI(Player player, BuyOrder order) {
        ModifyBuyOrderGUI modifyGUI = new ModifyBuyOrderGUI(plugin, order, player);
        playerModifyBuyOrderGUIs.put(player.getUniqueId(), modifyGUI);
        modifyGUI.open();
    }
    
    private void openBuyOrderDetailGUI(Player player, BuyOrder buyOrder) {
        BuyOrderDetailGUI buyOrderDetailGUI = new BuyOrderDetailGUI(plugin, buyOrder, player);
        playerBuyOrderDetailGUIs.put(player.getUniqueId(), buyOrderDetailGUI);
        buyOrderDetailGUI.open();
    }

    private void performSellToBuyOrder(Player seller, BuyOrderGUI buyOrderGUI, BuyOrder order) {
        if (order == null) {
            seller.sendMessage("§c收购订单不存在！");
            return;
        }

        BuyOrderDetailGUI detailGUI = new BuyOrderDetailGUI(plugin, order, seller);
        playerBuyOrderDetailGUIs.put(seller.getUniqueId(), detailGUI);
        detailGUI.open();
    }

    private int countPlayerItemAmount(Player player, ItemStack targetItem) {
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

    private boolean removeItemsFromInventory(Player player, ItemStack targetItem, int amount) {
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

    private String getItemDisplayName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        }

        String i18nName = itemStack.getI18NDisplayName();
        return i18nName != null ? i18nName : itemStack.getType().name();
    }
    
    private void performDelistItem(Player player, MarketItem item, int delistAmount) {
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
                
                org.playermarket.model.WarehouseItem warehouseItem = new org.playermarket.model.WarehouseItem(
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
                
                org.playermarket.model.WarehouseItem warehouseItem = new org.playermarket.model.WarehouseItem(
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
                player.sendMessage("§a=== 下架成功 ===");
                player.sendMessage("§7商品: §e" + itemStack.getType().name() + " x" + delistAmount);
                player.sendMessage("§7已拆分为 §e" + totalGroups + " §7组存入仓库");
                
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
                player.sendMessage("§c下架部分成功！成功存入 §e" + successCount + " / " + totalGroups + " §7组");
            }
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage("§c下架失败！商品可能已被购买或不存在");
        }
    }
    
    private void withdrawWarehouseItem(Player player, org.playermarket.model.WarehouseItem item, int clickedSlot) {
        ItemStack sample = item.getItemStack();
        int totalAmount = item.getAmount();
        int maxStackSize = Math.min(sample.getMaxStackSize(), 99);

        int canFit = countCanFit(player, sample, maxStackSize);
        if (canFit <= 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage("§c背包已满！请清理后再取出");
            return;
        }

        int takeAmount = Math.min(totalAmount, canFit);

        int remaining = takeAmount;
        java.util.List<ItemStack> toGive = new java.util.ArrayList<>();
        while (remaining > 0) {
            int give = Math.min(remaining, maxStackSize);
            ItemStack stack = sample.clone();
            stack.setAmount(give);
            toGive.add(stack);
            remaining -= give;
        }

        java.util.Map<Integer, ItemStack> leftover = new java.util.HashMap<>();
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
            player.sendMessage("§c背包空间不足，无法取出");
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
            player.sendMessage("§c取出失败！数据库错误");
            return;
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        if (givenAmount >= totalAmount) {
            player.sendMessage("§a=== 取出成功 ===");
            player.sendMessage("§7物品: §e" + sample.getType().name() + " x" + totalAmount);
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top != null) {
                top.setItem(clickedSlot, new ItemStack(org.bukkit.Material.AIR));
            }
        } else {
            int left = totalAmount - givenAmount;
            player.sendMessage("§a=== 部分取出成功 ===");
            player.sendMessage("§7物品: §e" + sample.getType().name() + " x" + givenAmount);
            player.sendMessage("§7剩余: §e" + left + " 个");
            player.sendMessage("§c背包空间不足，请清理后继续取出");

            Inventory top = player.getOpenInventory().getTopInventory();
            if (top != null) {
                ItemStack display = top.getItem(clickedSlot);
                if (display != null && !display.getType().isAir()) {
                    ItemMeta meta = display.getItemMeta();
                    if (meta != null) {
                        java.util.List<String> lore = meta.getLore();
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

    private int countCanFit(Player player, ItemStack sample, int maxStackSize) {
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
    
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 异步获取并显示未读通知
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            java.util.List<String> notifications = dbManager.getUnreadNotifications(player.getUniqueId());
            
            if (!notifications.isEmpty()) {
                // 在主线程中显示通知
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    player.sendMessage("");
                    player.sendMessage("§6=== 未读通知 ===");
                    player.sendMessage("");
                    for (String notification : notifications) {
                        player.sendMessage(notification);
                    }
                    player.sendMessage("");
                    player.sendMessage("§7共 §e" + notifications.size() + " §7条通知");
                    player.sendMessage("");
                    
                    // 标记通知为已读
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        dbManager.markNotificationsAsRead(player.getUniqueId());
                    });
                });
            }
        });
    }
}