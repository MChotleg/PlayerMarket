package org.playermarket.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.playermarket.PlayerMarket;
import org.playermarket.database.DatabaseManager;
import org.playermarket.economy.EconomyManager;
import org.playermarket.model.MarketItem;
import org.playermarket.utils.I18n;

public class SellCommand implements CommandExecutor {
    private final PlayerMarket plugin;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    
    public SellCommand(PlayerMarket plugin) {
        this.plugin = plugin;
        this.dbManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18n.get("error.player_only"));
            return false;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("playermarket.use")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }
        
        if (args.length < 2 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage(I18n.get(player, "sell.help.title"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "sell.help.usage"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "sell.help.params"));
            player.sendMessage(I18n.get(player, "sell.help.param.amount"));
            player.sendMessage(I18n.get(player, "sell.help.param.price"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "sell.help.examples"));
            player.sendMessage(I18n.get(player, "sell.help.example.1"));
            player.sendMessage(I18n.get(player, "sell.help.example.2"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "sell.help.tip"));
            return false;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(I18n.get(player, "sell.invalid.amount"));
            return false;
        }
        
        if (amount <= 0) {
            player.sendMessage(I18n.get(player, "sell.amount.too_small"));
            return false;
        }
        
        double unitPrice;
        try {
            unitPrice = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(I18n.get(player, "sell.invalid.price"));
            return false;
        }
        
        if (unitPrice <= 0) {
            player.sendMessage(I18n.get(player, "sell.price.too_small"));
            return false;
        }
        
        double minPrice = plugin.getConfig().getDouble("transaction.min-price", 0.01);
        double maxPrice = plugin.getConfig().getDouble("transaction.max-price", 1000000.0);
        
        if (unitPrice < minPrice) {
            player.sendMessage(I18n.get(player, "sell.price.too_low", economyManager.format(minPrice)));
            return false;
        }
        
        if (unitPrice > maxPrice) {
            player.sendMessage(I18n.get(player, "sell.price.too_high", economyManager.format(maxPrice)));
            return false;
        }
        
        double totalPrice = amount * unitPrice;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(I18n.get(player, "sell.no_item"));
            return false;
        }
        
        int availableAmount = countItemInInventory(player, item);
        if (availableAmount < amount) {
            player.sendMessage(I18n.get(player, "sell.insufficient.stock"));
            player.sendMessage(I18n.get(player, "sell.stock.need", amount));
            player.sendMessage(I18n.get(player, "sell.stock.have", availableAmount));
            player.sendMessage(I18n.get(player, "sell.stock.item", I18n.getItemDisplayName(item)));
            return false;
        }
        
        ItemStack itemToSell = item.clone();
        itemToSell.setAmount(1);
        
        MarketItem marketItem = new MarketItem(
            player.getUniqueId(),
            player.getName(),
            itemToSell,
            unitPrice,
            amount,
            totalPrice
        );
        
        if (dbManager.addMarketItem(marketItem)) {
            removeItemFromInventory(player, item, amount);
            
            String itemName = I18n.getItemDisplayName(item);
            
            player.sendMessage(I18n.get(player, "sell.success.title"));
            player.sendMessage(I18n.get(player, "sell.success.item_id", marketItem.getId()));
            player.sendMessage(I18n.get(player, "sell.success.item", itemName + " x" + amount));
            player.sendMessage(I18n.get(player, "sell.success.amount", amount));
            player.sendMessage(I18n.get(player, "sell.success.unit_price", economyManager.format(unitPrice)));
            player.sendMessage(I18n.get(player, "sell.success.total_price", economyManager.format(totalPrice)));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "sell.success.tip"));
            
            plugin.getLogger().info(I18n.get("sell.log.created", player.getName(), I18n.stripColorCodes(itemName), amount, String.valueOf(unitPrice)));
            return true;
        } else {
            player.sendMessage(I18n.get(player, "sell.failed"));
            return false;
        }
    }
    
    private int countItemInInventory(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
            }
        }
        return count;
    }
    
    private void removeItemFromInventory(Player player, ItemStack item, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack invItem = contents[i];
            if (invItem != null && invItem.isSimilar(item)) {
                int invAmount = invItem.getAmount();
                
                if (invAmount <= remaining) {
                    player.getInventory().setItem(i, null);
                    remaining -= invAmount;
                } else {
                    invItem.setAmount(invAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        player.updateInventory();
    }
}