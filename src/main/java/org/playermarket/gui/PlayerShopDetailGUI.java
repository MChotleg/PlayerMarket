package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerShopDetailGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final Player player;
    private final UUID shopOwnerUuid;
    private final String shopOwnerName;
    private final Inventory inventory;
    
    public PlayerShopDetailGUI(PlayerMarket plugin, Player player, UUID shopOwnerUuid, String shopOwnerName) {
        this.plugin = plugin;
        this.player = player;
        this.shopOwnerUuid = shopOwnerUuid;
        this.shopOwnerName = shopOwnerName;
        this.inventory = Bukkit.createInventory(this, 54, I18n.get(player, "player_shop.detail.title", shopOwnerName));
        createInventory();
    }
    
    private void createInventory() {
        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, blackPane);
        }
        
        createShopHeader();
        createButtons();
    }
    
    private void createShopHeader() {
        ItemStack shopHeader = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta headerMeta = shopHeader.getItemMeta();
        if (headerMeta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) headerMeta;
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(shopOwnerUuid);
            skullMeta.setOwningPlayer(offlinePlayer);
            skullMeta.setDisplayName(I18n.get(player, "player_shop.detail.shop_name", shopOwnerName));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "player_shop.detail.owner", shopOwnerName));
            lore.add("");
            lore.add(I18n.get(player, "player_shop.detail.click_hint"));
            skullMeta.setLore(lore);
            shopHeader.setItemMeta(skullMeta);
        }
        inventory.setItem(4, shopHeader);
    }
    
    private void createButtons() {
        createListingsButton();
        createBuyOrdersButton();
        createBackButton();
    }
    
    private void createListingsButton() {
        ItemStack listingsButton = new ItemStack(Material.CHEST);
        ItemMeta listingsMeta = listingsButton.getItemMeta();
        if (listingsMeta != null) {
            listingsMeta.setDisplayName(I18n.get(player, "player_shop.detail.listings.button"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "player_shop.detail.listings.lore"));
            lore.add("");
            lore.add(I18n.get(player, "player_shop.detail.listings.click_hint"));
            listingsMeta.setLore(lore);
            listingsButton.setItemMeta(listingsMeta);
        }
        inventory.setItem(22, listingsButton);
    }
    
    private void createBuyOrdersButton() {
        ItemStack buyOrdersButton = new ItemStack(Material.GOLD_INGOT);
        ItemMeta buyOrdersMeta = buyOrdersButton.getItemMeta();
        if (buyOrdersMeta != null) {
            buyOrdersMeta.setDisplayName(I18n.get(player, "player_shop.detail.buy_orders.button"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "player_shop.detail.buy_orders.lore"));
            lore.add("");
            lore.add(I18n.get(player, "player_shop.detail.buy_orders.click_hint"));
            buyOrdersMeta.setLore(lore);
            buyOrdersButton.setItemMeta(buyOrdersMeta);
        }
        inventory.setItem(31, buyOrdersButton);
    }
    
    private void createBackButton() {
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(I18n.get(player, "gui.back.to.player_shop"));
            backMeta.setLore(lore);
            backButton.setItemMeta(backMeta);
        }
        inventory.setItem(49, backButton);
    }
    
    public void open() {
        player.openInventory(inventory);
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    
    public UUID getShopOwnerUuid() {
        return shopOwnerUuid;
    }
    
    public String getShopOwnerName() {
        return shopOwnerName;
    }
    
    public void cleanup() {
    }
}
