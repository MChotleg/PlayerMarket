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
import org.playermarket.model.BuyOrder;
import org.playermarket.utils.I18n;

public class PurCommand implements CommandExecutor {
    private final PlayerMarket plugin;
    private final DatabaseManager dbManager;
    private final EconomyManager economyManager;
    
    public PurCommand(PlayerMarket plugin) {
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
            player.sendMessage(I18n.get(player, "pur.help.title"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "pur.help.usage"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "pur.help.params"));
            player.sendMessage(I18n.get(player, "pur.help.param.amount"));
            player.sendMessage(I18n.get(player, "pur.help.param.price"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "pur.help.examples"));
            player.sendMessage(I18n.get(player, "pur.help.example.1"));
            player.sendMessage(I18n.get(player, "pur.help.example.2"));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "pur.help.tip"));
            return false;
        }
        
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(I18n.get(player, "pur.invalid.amount"));
            return false;
        }
        
        if (amount <= 0) {
            player.sendMessage(I18n.get(player, "pur.amount.too_small"));
            return false;
        }
        
        double unitPrice;
        try {
            unitPrice = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(I18n.get(player, "pur.invalid.price"));
            return false;
        }
        
        if (unitPrice <= 0) {
            player.sendMessage(I18n.get(player, "pur.price.too_small"));
            return false;
        }
        
        double minPrice = plugin.getConfig().getDouble("transaction.min-price", 0.01);
        double maxPrice = plugin.getConfig().getDouble("transaction.max-price", 1000000.0);
        
        if (unitPrice < minPrice) {
            player.sendMessage(I18n.get(player, "pur.price.too_low", economyManager.format(minPrice)));
            return false;
        }
        
        if (unitPrice > maxPrice) {
            player.sendMessage(I18n.get(player, "pur.price.too_high", economyManager.format(maxPrice)));
            return false;
        }
        
        double totalPrice = unitPrice * amount;
        
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(I18n.get(player, "pur.no_item"));
            return false;
        }
        
        if (economyManager.getBalance(player) < totalPrice) {
            player.sendMessage(I18n.get(player, "pur.insufficient.balance", economyManager.format(totalPrice)));
            player.sendMessage(I18n.get(player, "pur.current.balance", economyManager.format(economyManager.getBalance(player))));
            return false;
        }
        
        if (!economyManager.withdraw(player, totalPrice)) {
            player.sendMessage(I18n.get(player, "pur.withdraw.failed"));
            return false;
        }
        
        BuyOrder buyOrder = new BuyOrder(
            player.getUniqueId(),
            player.getName(),
            item,
            unitPrice,
            amount,
            totalPrice
        );
        
        if (dbManager.addBuyOrder(buyOrder)) {
            String itemName = I18n.getItemDisplayName(item);
            
            player.sendMessage(I18n.get(player, "pur.success.title"));
            player.sendMessage(I18n.get(player, "pur.success.order_id", buyOrder.getId()));
            player.sendMessage(I18n.get(player, "pur.success.item", itemName + " x" + amount));
            player.sendMessage(I18n.get(player, "pur.success.amount", amount));
            player.sendMessage(I18n.get(player, "pur.success.unit_price", economyManager.format(unitPrice)));
            player.sendMessage(I18n.get(player, "pur.success.total_price", economyManager.format(totalPrice)));
            player.sendMessage("");
            player.sendMessage(I18n.get(player, "pur.success.tip"));
            
            plugin.getLogger().info(I18n.get("pur.log.created", player.getName(), I18n.stripColorCodes(itemName), amount, String.valueOf(unitPrice)));
            return true;
        } else {
            economyManager.deposit(player, totalPrice);
            player.sendMessage(I18n.get(player, "pur.failed"));
            return false;
        }
    }
}