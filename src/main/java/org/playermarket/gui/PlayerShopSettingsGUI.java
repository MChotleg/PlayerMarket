package org.playermarket.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;

public class PlayerShopSettingsGUI implements InventoryHolder {
    private final PlayerMarket plugin;
    private final Player player;
    private Inventory inventory;

    public PlayerShopSettingsGUI(PlayerMarket plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9, I18n.get(player, "player_shop.settings.title"));
        build();
    }

    private boolean isOpenStatus() {
        String path = "player-shops.open-status." + player.getUniqueId();
        return plugin.getConfig().getBoolean(path, true);
    }

    private void build() {
        inventory.clear();

        ItemStack blackPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackPane.getItemMeta();
        if (blackMeta != null) {
            blackMeta.setDisplayName(" ");
            blackPane.setItemMeta(blackMeta);
        }
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, blackPane);
        }

        boolean open = isOpenStatus();
        String statusText = I18n.get(player, open ? "player_shop.status.open" : "player_shop.status.closed");

        // 开始营业（槽位3）
        ItemStack openItem = new ItemStack(Material.GREEN_DYE);
        ItemMeta openMeta = openItem.getItemMeta();
        if (openMeta != null) {
            openMeta.setDisplayName(I18n.get(player, "player_shop.settings.open_button"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "player_shop.settings.open_lore"));
            openMeta.setLore(lore);
            openItem.setItemMeta(openMeta);
        }
        inventory.setItem(3, openItem);

        // 当前状态（槽位4）
        ItemStack statusItem = new ItemStack(Material.NAME_TAG);
        ItemMeta statusMeta = statusItem.getItemMeta();
        if (statusMeta != null) {
            statusMeta.setDisplayName(I18n.get(player, "player_shop.settings.status", statusText));
            statusItem.setItemMeta(statusMeta);
        }
        inventory.setItem(4, statusItem);

        // 打烊（槽位5）
        ItemStack closeItem = new ItemStack(Material.RED_DYE);
        ItemMeta closeMeta = closeItem.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(I18n.get(player, "player_shop.settings.close_button"));
            List<String> lore = new ArrayList<>();
            lore.add(I18n.get(player, "player_shop.settings.close_lore"));
            closeMeta.setLore(lore);
            closeItem.setItemMeta(closeMeta);
        }
        inventory.setItem(5, closeItem);

        // 返回（槽位8）
        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(I18n.get(player, "gui.back"));
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(I18n.get(player, "gui.back.to.main"));
            backMeta.setLore(lore);
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(8, backItem);
    }

    public void open() {
        player.openInventory(inventory);
    }

    public void refresh() {
        build();
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}