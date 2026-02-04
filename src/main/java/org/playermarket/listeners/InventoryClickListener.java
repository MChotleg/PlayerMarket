package org.playermarket.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Material;
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
import org.playermarket.gui.*;
import org.playermarket.gui.PlayerShopListingsGUI;
import org.playermarket.gui.PlayerShopBuyOrdersGUI;
import org.playermarket.model.MarketItem;
import org.playermarket.model.WarehouseItem;
import org.playermarket.model.BuyOrder;
import org.playermarket.utils.I18n;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 处理库存相关事件的监听器
 * 包括库存点击、拖拽、创造模式事件、丢弃物品和关闭事件
 */
public class InventoryClickListener extends BaseMarketListener implements Listener {
    
    public InventoryClickListener(PlayerMarket plugin) {
        super(plugin);
    }
    
    private void handlePlayerShopListingsGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayerShopListingsGUI)) {
            return;
        }
        
        PlayerShopListingsGUI listingsGUI = (PlayerShopListingsGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 45) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            listingsGUI.setCurrentPage(listingsGUI.getCurrentPage() - 1);
            listingsGUI.refresh();
            return;
        } else if (slot == 46) {
            // 返回按钮：返回店铺详情
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopDetailGUI detailGUI = new PlayerShopDetailGUI(plugin, player, listingsGUI.getShopOwnerUuid(), listingsGUI.getShopOwnerName());
            playerPlayerShopDetailGUIs.put(player.getUniqueId(), detailGUI);
            detailGUI.open();
            return;
        } else if (slot == 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            listingsGUI.setCurrentPage(listingsGUI.getCurrentPage() + 1);
            listingsGUI.refresh();
            return;
        } else if (slot == 49) {
            // 刷新页面
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            listingsGUI.refresh();
            return;
        }
        
        if (slot >= 0 && slot < 45) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            MarketItem item = null;

            List<MarketItem> items = listingsGUI.getMarketItems();
            int index = (listingsGUI.getCurrentPage() - 1) * 45 + slot;
            if (items != null && index >= 0 && index < items.size()) {
                item = items.get(index);
            }

            if (item == null) {
                Integer itemId = extractMarketItemId(clickedItem);
                if (itemId != null) {
                    item = dbManager.getItemById(itemId);
                }
            }

            if (item == null || item.isSold()) {
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                listingsGUI.refresh();
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            ItemDetailGUI detailGUI = new ItemDetailGUI(plugin, item);
            detailGUI.setBackAction(() -> {
                playerPlayerShopListingsGUIs.put(player.getUniqueId(), listingsGUI);
                listingsGUI.open();
            });
            playerDetailGUIs.put(player.getUniqueId(), detailGUI);
            detailGUI.open(player);
        }
    }

    private void handlePlayerShopBuyOrdersGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayerShopBuyOrdersGUI)) {
            return;
        }
        
        PlayerShopBuyOrdersGUI buyOrdersGUI = (PlayerShopBuyOrdersGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 45) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            buyOrdersGUI.setCurrentPage(buyOrdersGUI.getCurrentPage() - 1);
            buyOrdersGUI.refresh();
            return;
        } else if (slot == 46) {
            // 返回按钮：返回店铺详情
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopDetailGUI detailGUI = new PlayerShopDetailGUI(plugin, player, buyOrdersGUI.getShopOwnerUuid(), buyOrdersGUI.getShopOwnerName());
            playerPlayerShopDetailGUIs.put(player.getUniqueId(), detailGUI);
            detailGUI.open();
            return;
        } else if (slot == 53) {
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            buyOrdersGUI.setCurrentPage(buyOrdersGUI.getCurrentPage() + 1);
            buyOrdersGUI.refresh();
            return;
        } else if (slot == 49) {
            // 刷新
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            buyOrdersGUI.refresh();
            return;
        }
        
        if (slot >= 0 && slot < 45) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) {
                return;
            }

            BuyOrder order = null;

            List<BuyOrder> orders = buyOrdersGUI.getBuyOrders();
            int index = (buyOrdersGUI.getCurrentPage() - 1) * 28 + slot;
            if (orders != null && index >= 0 && index < orders.size()) {
                order = orders.get(index);
            }

            if (order == null) {
                Integer orderId = extractBuyOrderId(clickedItem);
                if (orderId != null) {
                    order = dbManager.getBuyOrderById(orderId);
                }
            }

            if (order == null || order.isFulfilled()) {
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                buyOrdersGUI.refresh();
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

            try {
                BuyOrderDetailGUI detailGUI = new BuyOrderDetailGUI(plugin, order, player);
                detailGUI.setBackAction(() -> {
                    playerPlayerShopBuyOrdersGUIs.put(player.getUniqueId(), buyOrdersGUI);
                    buyOrdersGUI.open();
                });
                playerBuyOrderDetailGUIs.put(player.getUniqueId(), detailGUI);
                detailGUI.open();
            } catch (Exception e) {
                plugin.getLogger().severe(I18n.get("marketlistener.log.open_detail_failed", e.getMessage()));
                e.printStackTrace();
                player.sendMessage(I18n.get(player, "marketlistener.open_detail_failed", e.getMessage()));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();

        if (inventory == null || inventory.getHolder() == null) {
            return;
        }

        if (isCustomGUI(inventory)) {
            event.setCancelled(true);
        }
        
        if (inventory.getHolder() instanceof MarketGUI) {
            
            int slot = event.getSlot();
            
            if (slot == 11) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMarketItemsGUI(player);
                return;
            } else if (slot == 13) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openBuyOrderGUI(player);
                return;
            } else if (slot == 15) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openPlayerShopGUI(player);
                return;
            } else if (slot == 29) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMyListingsGUI(player);
                return;
            } else if (slot == 31) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openMyBuyOrdersGUI(player);
                return;
            } else if (slot == 33) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                openWarehouseGUI(player);
                return;
            } else if (slot == 49) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                return;
            }
        }
        
        handleMarketItemsGUIClick(event, player, inventory);
        handleBuyOrderGUIClick(event, player, inventory);
        handleMyBuyOrdersGUIClick(event, player, inventory);
        handleBuyOrderDetailGUIClick(event, player, inventory);
        handleMyListingsGUIClick(event, player, inventory);
        handleDelistingDetailGUIClick(event, player, inventory);
        handleItemDetailGUIClick(event, player, inventory);
        handleWarehouseGUIClick(event, player, inventory);
        handleModifyBuyOrderGUIClick(event, player, inventory);
        handlePlayerShopGUIClick(event, player, inventory);
        handlePlayerShopSettingsGUIClick(event, player, inventory);
        handlePlayerShopDetailGUIClick(event, player, inventory);
        handlePlayerShopListingsGUIClick(event, player, inventory);
        handlePlayerShopBuyOrdersGUIClick(event, player, inventory);
        handleAllPlayerShopsGUIClick(event, player, inventory);
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
        
        if (event.getInventory().getHolder() instanceof PlayerShopGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                PlayerShopGUI playerShopGUI = playerPlayerShopGUIs.remove(player.getUniqueId());
                if (playerShopGUI != null) {
                    playerShopGUI.cleanup();
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof AllPlayerShopsGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                AllPlayerShopsGUI allPlayerShopsGUI = playerAllPlayerShopsGUIs.remove(player.getUniqueId());
                if (allPlayerShopsGUI != null) {
                    allPlayerShopsGUI.cleanup();
                }
            }
        }
        
        if (event.getInventory().getHolder() instanceof PlayerShopDetailGUI) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                PlayerShopDetailGUI detailGUI = playerPlayerShopDetailGUIs.remove(player.getUniqueId());
                if (detailGUI != null) {
                    detailGUI.cleanup();
                }
            }
        }
    }

    private void handleMarketItemsGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof MarketItemsGUI)) {
            return;
        }
        
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

            MarketItem item = null;

            List<MarketItem> items = marketItemsGUI.getMarketItems();
            int index = (marketItemsGUI.getCurrentPage() - 1) * 45 + slot;
            if (items != null && index >= 0 && index < items.size()) {
                item = items.get(index);
            }

            if (item == null) {
                Integer itemId = extractMarketItemId(clickedItem);
                if (itemId != null) {
                    item = dbManager.getItemById(itemId);
                }
            }

            if (item == null || item.isSold()) {
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                marketItemsGUI.refresh();
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            ItemDetailGUI detailGUI = new ItemDetailGUI(plugin, item);
            playerDetailGUIs.put(player.getUniqueId(), detailGUI);
            detailGUI.open(player);
        }
    }
    
    private void handleBuyOrderGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof BuyOrderGUI)) {
            return;
        }
        
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

            BuyOrder order = null;

            List<BuyOrder> orders = buyOrderGUI.getBuyOrders();
            int index = (buyOrderGUI.getCurrentPage() - 1) * 28 + slot;
            if (orders != null && index >= 0 && index < orders.size()) {
                order = orders.get(index);
            }

            if (order == null) {
                Integer orderId = extractBuyOrderId(clickedItem);
                if (orderId != null) {
                    order = dbManager.getBuyOrderById(orderId);
                }
            }

            if (order == null || order.isFulfilled()) {
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                buyOrderGUI.refresh();
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);

            try {
                BuyOrderDetailGUI detailGUI = new BuyOrderDetailGUI(plugin, order, player);
                playerBuyOrderDetailGUIs.put(player.getUniqueId(), detailGUI);
                detailGUI.open();
            } catch (Exception e) {
                plugin.getLogger().severe(I18n.get("marketlistener.log.open_detail_failed", e.getMessage()));
                e.printStackTrace();
                player.sendMessage(I18n.get(player, "marketlistener.open_detail_failed", e.getMessage()));
            }
        }
    }
    
    private void handleMyBuyOrdersGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof MyBuyOrdersGUI)) {
            return;
        }

        MyBuyOrdersGUI myBuyOrdersGUI = (MyBuyOrdersGUI) inventory.getHolder();
        int slot = event.getSlot();

        if (slot == 45) {
            // 上一页
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
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
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != org.bukkit.Material.ARROW) {
                return;
            }
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
                    player.sendMessage(I18n.get(player, "marketlistener.order_not_exist"));
                    myBuyOrdersGUI.refresh();
                    return;
                }
                
                plugin.getLogger().info(I18n.get("marketlistener.log.order_check", latestOrder.getId(), latestOrder.isFulfilled(), latestOrder.getAmount(), latestOrder.getRemainingAmount()));
                
                int acquiredAmount = latestOrder.getAmount() - latestOrder.getRemainingAmount();
                boolean isPartiallyFulfilled = acquiredAmount > 0 && !latestOrder.isFulfilled();
                
                if (event.getClick().isRightClick()) {
                    if (!latestOrder.isFulfilled()) {
                        boolean cancelled = dbManager.cancelBuyOrder(latestOrder.getId(), player.getUniqueId());
                        if (cancelled) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                            player.sendMessage(I18n.get(player, "marketlistener.order_cancelled"));
                            myBuyOrdersGUI.refresh();
                        } else {
                            player.sendMessage(I18n.get(player, "marketlistener.cancel_failed"));
                        }
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                        player.sendMessage(I18n.get(player, "marketlistener.order_cant_cancel"));
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
                                player.sendMessage(I18n.get(player, "marketlistener.items_transferred"));
                                myBuyOrdersGUI.refresh();
                            } else {
                                player.sendMessage(I18n.get(player, "marketlistener.transfer_failed"));
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                            player.sendMessage(I18n.get(player, "marketlistener.no_items"));
                        }
                    } else if (isPartiallyFulfilled) {
                        if (acquiredAmount > 0) {
                            boolean claimed = dbManager.claimBuyOrderAcquiredItems(latestOrder.getId(), player.getUniqueId(), acquiredAmount);
                            if (!claimed) {
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                                player.sendMessage(I18n.get(player, "marketlistener.no_withdrawable"));
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
                                player.sendMessage(I18n.get(player, "marketlistener.items_transferred"));
                                myBuyOrdersGUI.refresh();
                            } else {
                                player.sendMessage(I18n.get(player, "marketlistener.transfer_failed"));
                                myBuyOrdersGUI.refresh();
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                            player.sendMessage(I18n.get(player, "marketlistener.no_items"));
                        }
                    } else {
                        // 未完成状态：修改收购数量
                        openModifyBuyOrderGUI(player, latestOrder);
                    }
                }
            }
        }
    }
    
    private void handleBuyOrderDetailGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof BuyOrderDetailGUI)) {
            return;
        }
        
        BuyOrderDetailGUI detailGUI = (BuyOrderDetailGUI) inventory.getHolder();
        int slot = event.getSlot();

        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            BuyOrderDetailGUI stored = playerBuyOrderDetailGUIs.remove(player.getUniqueId());
            if (stored != null) {
                stored.cleanup(player);
            }
            
            if (detailGUI.getBackAction() != null) {
                detailGUI.getBackAction().run();
                return;
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
                player.sendMessage(I18n.get(player, "marketlistener.order_completed"));
                detailGUI.refresh();
                return;
            }

            // 禁止向自己的收购订单出售（管理员除外）
            if (player.getUniqueId().equals(order.getBuyerUuid()) && !player.hasPermission("playermarket.admin")) {
                player.sendMessage(I18n.get(player, "marketlistener.cannot_fulfill_own_order"));
                return;
            }

            int sellAmount = BuyOrderDetailGUI.getPlayerSellAmounts().getOrDefault(player.getUniqueId(), 1);
            int max = Math.min(order.getRemainingAmount(), countPlayerItemAmount(player, order.getItemStack()));
            if (max <= 0) {
                    player.sendMessage(I18n.get(player, "trade.insufficient.stock"));
                    return;
                }

                sellAmount = Math.min(sellAmount, max);
                if (sellAmount <= 0) {
                    player.sendMessage(I18n.get(player, "marketlistener.invalid_sell_amount"));
                    return;
                }

            double requiredAmount = sellAmount * order.getUnitPrice();
            double buyerBalance = economyManager.getBalance(order.getBuyerUuid());
            if (buyerBalance < requiredAmount) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                player.sendMessage(I18n.get(player, "error.economy"));
                player.sendMessage(I18n.get(player, "marketlistener.buyer_balance_needed", economyManager.format(requiredAmount)));
                player.sendMessage(I18n.get(player, "marketlistener.buyer_balance", economyManager.format(buyerBalance)));
                return;
            }

            ItemStack targetItem = order.getItemStack();
            boolean removed = removeItemsFromInventory(player, targetItem, sellAmount);
            if (!removed) {
                player.sendMessage(I18n.get(player, "marketlistener.remove_item_failed"));
                return;
            }

            boolean ok = dbManager.sellToBuyOrder(order.getId(), player.getUniqueId(), player.getName(), sellAmount);
            if (!ok) {
                ItemStack rollback = targetItem.clone();
                rollback.setAmount(sellAmount);
                player.getInventory().addItem(rollback);
                player.updateInventory();
                player.sendMessage(I18n.get(player, "marketlistener.sell_failed"));
                detailGUI.refresh();
                return;
            }

            double income = sellAmount * order.getUnitPrice();
            
            boolean withdrawn = economyManager.withdraw(order.getBuyerUuid(), income);
            if (!withdrawn) {
                ItemStack rollback = targetItem.clone();
                rollback.setAmount(sellAmount);
                player.getInventory().addItem(rollback);
                player.updateInventory();
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                player.sendMessage(I18n.get(player, "marketlistener.buyer_insufficient"));
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

            player.sendMessage(I18n.get(player, "trade.sell.success"));
            player.sendMessage(I18n.get(player, "marketlistener.item_sold", itemName + " x" + soldAmount));
            player.sendMessage(I18n.get(player, "trade.sell.received", economyManager.format(soldIncome)));

            // 记录交易日志
            String executorIp = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
            dbManager.logTransaction(order.getBuyerUuid(), player.getUniqueId(), I18n.stripColorCodes(itemName), soldAmount, soldIncome, "ORDER_FILL", player.getUniqueId(), executorIp);

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                String notificationMessage = "§a收购订单成交 §7| §e" + itemName + " x" + soldAmount +
                        " §7| §e支付: " + economyManager.format(soldIncome) + " §7| 卖家: " + sellerName;
                String notificationData = itemName + "," + soldAmount + "," + soldIncome + "," + sellerName;
                int notificationId = dbManager.addNotification(buyerUuid, buyerName, "buyorder", notificationMessage, notificationData);

                Player buyer = plugin.getServer().getPlayer(buyerUuid);
                if (buyer != null && buyer.isOnline()) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        buyer.playSound(buyer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                        buyer.sendMessage(I18n.get(buyer, "marketlistener.buyorder_completed"));
                        buyer.sendMessage(I18n.get(buyer, "marketlistener.buyorder_item", itemName + " x" + soldAmount));
                        buyer.sendMessage(I18n.get(buyer, "marketlistener.buyorder_seller", sellerName));
                        buyer.sendMessage(I18n.get(buyer, "marketlistener.buyorder_paid", economyManager.format(soldIncome)));
                    });
                    
                    // 买家在线，立即标记通知为已读
                    if (notificationId > 0) {
                        dbManager.markNotificationAsRead(notificationId);
                    }
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
    
    private void handleMyListingsGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof MyListingsGUI)) {
            return;
        }
        
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
                    player.sendMessage(I18n.get(player, "error.item.notfound"));
                    return;
                }

                MarketItem item = dbManager.getItemById(itemId);
                if (item == null || item.isSold() || !player.getUniqueId().equals(item.getSellerUuid())) {
                    player.sendMessage(I18n.get(player, "error.item.notfound"));
                    return;
                }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openDelistingDetailGUI(player, item);
        }
    }
    
    private void handleDelistingDetailGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof DelistingDetailGUI)) {
            return;
        }
        
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
    
    private void handleItemDetailGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof ItemDetailGUI)) {
            return;
        }
        
        ItemDetailGUI detailGUI = (ItemDetailGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            
            if (detailGUI.getBackAction() != null) {
                detailGUI.getBackAction().run();
                return;
            }
            
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
    
    private void handleWarehouseGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof WarehouseGUI)) {
            return;
        }
        
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
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                return;
            }

            WarehouseItem item = dbManager.getWarehouseItemById(itemId);
            if (item == null || !player.getUniqueId().equals(item.getOwnerUuid())) {
                player.sendMessage(I18n.get(player, "error.item.notfound"));
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            withdrawWarehouseItem(player, item, slot);
        }
    }
    
    private void handleModifyBuyOrderGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof ModifyBuyOrderGUI)) {
            return;
        }
        
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
                player.sendMessage(I18n.get(player, "marketlistener.modify_success"));
                
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
                player.sendMessage(I18n.get(player, "marketlistener.modify_failed"));
            }
            return;
        }
    }
    
    private void purchaseItem(Player player, MarketItem item, int purchaseAmount) {
        if (item == null) {
            player.sendMessage(I18n.get(player, "marketlistener.item_not_exist"));
            return;
        }
        
        // 检查店铺是否打烊
        boolean isOpen = plugin.getConfig().getBoolean("player-shops.open-status." + item.getSellerUuid(), true);
        if (!isOpen) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "shop.closed.purchase_denied"));
            return;
        }
        
        if (item.isSold()) {
            player.sendMessage(I18n.get(player, "marketlistener.item_already_sold"));
            return;
        }
        
        // 禁止购买自己的商品（管理员除外）
        if (player.getUniqueId().equals(item.getSellerUuid()) && !player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "marketlistener.cannot_buy_own_item"));
            return;
        }
        
        if (purchaseAmount <= 0 || purchaseAmount > item.getAmount()) {
            player.sendMessage(I18n.get(player, "marketlistener.invalid_purchase_amount"));
            return;
        }
        
        double unitPrice = item.getPrice() / item.getAmount();
        double totalPrice = unitPrice * purchaseAmount;
        
        double balance = economyManager.getBalance(player);
        
        if (balance < totalPrice) {
            player.sendMessage(I18n.get(player, "marketlistener.insufficient_funds"));
            player.sendMessage(I18n.get(player, "marketlistener.funds_needed", economyManager.format(totalPrice)));
            player.sendMessage(I18n.get(player, "marketlistener.funds_have", economyManager.format(balance)));
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
            final String itemName = I18n.getItemDisplayName(itemStack);
            
            // 给卖家加钱（无论卖家是否在线）
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                Player seller = plugin.getServer().getPlayer(item.getSellerUuid());
                
                // 无论卖家是否在线，都给卖家加钱
                economyManager.deposit(item.getSellerUuid(), totalPrice);
                
                // 添加通知到数据库
                String notificationMessage = "§a商品已售出 §7| §e" + itemName + " x" + purchaseAmount + " §7| §e" + economyManager.format(totalPrice);
                String notificationData = itemName + "," + purchaseAmount + "," + totalPrice;
                int notificationId = dbManager.addNotification(item.getSellerUuid(), item.getSellerName(), "sale", notificationMessage, notificationData);
                
                // 如果卖家在线，发送通知消息和播放音效
                if (seller != null && seller.isOnline()) {
                    seller.playSound(seller.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    seller.sendMessage(I18n.get(seller, "marketlistener.sold.title"));
                    seller.sendMessage(I18n.get(seller, "marketlistener.sold.buyer", player.getName()));
                    seller.sendMessage(I18n.get(seller, "marketlistener.sold.amount", purchaseAmount));
                    seller.sendMessage(I18n.get(seller, "marketlistener.sold.income", economyManager.format(totalPrice)));
                    
                    // 卖家在线，立即标记通知为已读
                    if (notificationId > 0) {
                        dbManager.markNotificationAsRead(notificationId);
                    }
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
                player.sendMessage(I18n.get(player, "marketlistener.purchase_failed"));
            }

            player.sendMessage(I18n.get(player, "marketlistener.purchase_success"));
            player.sendMessage(I18n.get(player, "marketlistener.purchase_item", itemName + " x" + purchaseAmount));
            player.sendMessage(I18n.get(player, "marketlistener.purchase_unit_price", economyManager.format(unitPrice)));
            player.sendMessage(I18n.get(player, "marketlistener.purchase_total", economyManager.format(totalPrice)));
            player.sendMessage(I18n.get(player, "marketlistener.purchase_transferred"));
            
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
            
            plugin.getLogger().info(I18n.get("marketlistener.log.purchase", player.getName(), I18n.stripColorCodes(I18n.getItemDisplayName(itemStack)), purchaseAmount));
            
            // 记录交易日志
            String executorIp = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
            dbManager.logTransaction(player.getUniqueId(), item.getSellerUuid(), I18n.stripColorCodes(itemName), purchaseAmount, totalPrice, "MARKET_BUY", player.getUniqueId(), executorIp);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(I18n.get(player, "marketlistener.purchase_failed2"));
        }
    }
    
    private void handlePlayerShopGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayerShopGUI)) {
            return;
        }
        
        PlayerShopGUI playerShopGUI = (PlayerShopGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 45) {
            // 返回按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopGUI storedGUI = playerPlayerShopGUIs.remove(player.getUniqueId());
            if (storedGUI != null) {
                storedGUI.cleanup();
            }
            marketGUI.openMarketGUI(player);
            return;
        } else if (slot == 53) {
            // 查看全服店铺按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openAllPlayerShopsGUI(player);
            return;
        } else if (slot == 49) {
            // 店铺设置按钮（玩家头像）
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openPlayerShopSettingsGUI(player);
            return;
        }
        
        // 处理推荐店铺点击（槽位19-25, 28-34）
        int[] featuredSlots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        for (int featuredSlot : featuredSlots) {
            if (slot == featuredSlot) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                
                // 获取点击的店铺信息
                PlayerShopGUI.FeaturedShopInfo shopInfo = playerShopGUI.getFeaturedShopInfo(slot);
                if (shopInfo != null) {
                    // 打开店铺详情界面
                    PlayerShopDetailGUI detailGUI = new PlayerShopDetailGUI(plugin, player, shopInfo.getPlayerUuid(), shopInfo.getPlayerName());
                    playerPlayerShopDetailGUIs.put(player.getUniqueId(), detailGUI);
                    detailGUI.open();
                } else {
                    player.sendMessage(I18n.get(player, "player_shop.featured.clicked"));
                }
                return;
            }
        }
    }
    
    private void handleAllPlayerShopsGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof AllPlayerShopsGUI)) {
            return;
        }
        
        AllPlayerShopsGUI allPlayerShopsGUI = (AllPlayerShopsGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 45) {
            // 上一页按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            allPlayerShopsGUI.previousPage();
            return;
        } else if (slot == 49) {
            // 返回按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            AllPlayerShopsGUI storedGUI = playerAllPlayerShopsGUIs.remove(player.getUniqueId());
            if (storedGUI != null) {
                storedGUI.cleanup();
            }
            openPlayerShopGUI(player);
            return;
        } else if (slot == 53) {
            // 下一页按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            allPlayerShopsGUI.nextPage();
            return;
        }
        
        // 处理店铺点击（槽位0-44）
        if (slot >= 0 && slot < 45) {
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                
                // 获取点击的店铺信息
                org.playermarket.model.PlayerShop shop = allPlayerShopsGUI.getShopBySlot(slot);
                if (shop != null) {
                    // 打开店铺详情界面
                    PlayerShopDetailGUI detailGUI = new PlayerShopDetailGUI(plugin, player, shop.getPlayerUuid(), shop.getPlayerName());
                    playerPlayerShopDetailGUIs.put(player.getUniqueId(), detailGUI);
                    detailGUI.open();
                } else {
                    player.sendMessage(I18n.get(player, "player_shop.all_shops.clicked"));
                }
                return;
            }
        }
    }
    
    private void performSellToBuyOrder(Player seller, BuyOrderGUI buyOrderGUI, BuyOrder order) {
        if (order == null) {
            seller.sendMessage(I18n.get(seller, "marketlistener.order_not_exist"));
            return;
        }

        BuyOrderDetailGUI detailGUI = new BuyOrderDetailGUI(plugin, order, seller);
        playerBuyOrderDetailGUIs.put(seller.getUniqueId(), detailGUI);
        detailGUI.open();
    }

    private void handlePlayerShopSettingsGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof org.playermarket.gui.PlayerShopSettingsGUI)) {
            return;
        }
        int slot = event.getSlot();
        if (slot == 8) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            openPlayerShopGUI(player);
            return;
        } else if (slot == 3 || slot == 5) {
            boolean toOpen = (slot == 3);
            String path = "player-shops.open-status." + player.getUniqueId();
            plugin.getConfig().set(path, toOpen);
            plugin.saveConfig();
            String status = I18n.get(player, toOpen ? "player_shop.status.open" : "player_shop.status.closed");
            player.sendMessage(I18n.get(player, "player_shop.settings.updated", status));
            org.playermarket.gui.PlayerShopSettingsGUI settingsGUI = (org.playermarket.gui.PlayerShopSettingsGUI) inventory.getHolder();
            settingsGUI.refresh();
            return;
        }
    }
    
    private void handlePlayerShopDetailGUIClick(InventoryClickEvent event, Player player, Inventory inventory) {
        if (!(inventory.getHolder() instanceof PlayerShopDetailGUI)) {
            return;
        }
        
        PlayerShopDetailGUI detailGUI = (PlayerShopDetailGUI) inventory.getHolder();
        int slot = event.getSlot();
        
        if (slot == 49) {
            // 返回按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopDetailGUI storedGUI = playerPlayerShopDetailGUIs.remove(player.getUniqueId());
            if (storedGUI != null) {
                storedGUI.cleanup();
            }
            openAllPlayerShopsGUI(player);
            return;
        } else if (slot == 22) {
            // 上架商品按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopListingsGUI listingsGUI = new PlayerShopListingsGUI(plugin, player, detailGUI.getShopOwnerUuid(), detailGUI.getShopOwnerName());
            playerPlayerShopListingsGUIs.put(player.getUniqueId(), listingsGUI);
            listingsGUI.open();
            return;
        } else if (slot == 31) {
            // 收购订单按钮
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
            PlayerShopBuyOrdersGUI buyOrdersGUI = new PlayerShopBuyOrdersGUI(plugin, player, detailGUI.getShopOwnerUuid(), detailGUI.getShopOwnerName());
            playerPlayerShopBuyOrdersGUIs.put(player.getUniqueId(), buyOrdersGUI);
            buyOrdersGUI.open();
            return;
        }
    }
}
