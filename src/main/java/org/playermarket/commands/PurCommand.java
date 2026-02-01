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
        // 检查是否为玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家才能使用该命令");
            return false;
        }
        
        Player player = (Player) sender;
        
        // 检查权限
        if (!player.hasPermission("playermarket.use")) {
            player.sendMessage("§c你没有权限使用此命令");
            return false;
        }
        
        // 检查参数
        if (args.length < 2 || args[0].equalsIgnoreCase("help")) {
            player.sendMessage("§6=== 玩家市场 - 收购帮助 ===");
            player.sendMessage("");
            player.sendMessage("§e用法: §f/pur <数量> <单价>");
            player.sendMessage("");
            player.sendMessage("§e参数说明:");
            player.sendMessage("§7<数量>  §f- 要收购的物品数量");
            player.sendMessage("§7<单价>  §f- 每个物品的收购价格");
            player.sendMessage("");
            player.sendMessage("§e示例:");
            player.sendMessage("§7/pur 64 100  §f- 收购64个物品，每个100元");
            player.sendMessage("§7/pur 10 50   §f- 收购10个物品，每个50元");
            player.sendMessage("");
            player.sendMessage("§e提示: §f手持要收购的物品后使用该命令");
            return false;
        }
        
        // 解析数量
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c无效的数量格式！请输入整数");
            return false;
        }
        
        // 验证数量
        if (amount <= 0) {
            player.sendMessage("§c数量必须大于0！");
            return false;
        }
        
        // 解析单价
        double unitPrice;
        try {
            unitPrice = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c无效的单价格式！请输入数字");
            return false;
        }
        
        // 验证单价
        if (unitPrice <= 0) {
            player.sendMessage("§c单价必须大于0！");
            return false;
        }
        
        double minPrice = plugin.getConfig().getDouble("transaction.min-price", 0.01);
        double maxPrice = plugin.getConfig().getDouble("transaction.max-price", 1000000.0);
        
        if (unitPrice < minPrice) {
            player.sendMessage("§c单价太低！最低单价: " + economyManager.format(minPrice));
            return false;
        }
        
        if (unitPrice > maxPrice) {
            player.sendMessage("§c单价太高！最高单价: " + economyManager.format(maxPrice));
            return false;
        }
        
        // 计算总价
        double totalPrice = unitPrice * amount;
        
        // 获取玩家手中的物品
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§c你手中没有物品！请先手持要收购的物品");
            return false;
        }
        
        // 检查余额是否足够
        if (economyManager.getBalance(player) < totalPrice) {
            player.sendMessage("§c余额不足！需要: " + economyManager.format(totalPrice));
            player.sendMessage("§c当前余额: " + economyManager.format(economyManager.getBalance(player)));
            return false;
        }
        
        // 扣款
        if (!economyManager.withdraw(player, totalPrice)) {
            player.sendMessage("§c扣款失败！请稍后重试");
            return false;
        }
        
        // 创建收购订单
        BuyOrder buyOrder = new BuyOrder(
            player.getUniqueId(),
            player.getName(),
            item,
            unitPrice,
            amount,
            totalPrice
        );
        
        // 添加到数据库
        if (dbManager.addBuyOrder(buyOrder)) {
            // 发送成功消息
            String itemName;
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemName = item.getItemMeta().getDisplayName();
            } else {
                itemName = item.getI18NDisplayName();
                if (itemName == null) {
                    itemName = item.getType().name();
                }
            }
            
            player.sendMessage("§a=== 收购订单创建成功 ===");
            player.sendMessage("§7订单ID: §e" + buyOrder.getId());
            player.sendMessage("§7物品: §e" + itemName + " x" + amount);
            player.sendMessage("§7数量: §e" + amount);
            player.sendMessage("§7单价: §e" + economyManager.format(unitPrice));
            player.sendMessage("§7总价: §e" + economyManager.format(totalPrice));
            player.sendMessage("");
            player.sendMessage("§a提示: 使用 /playermarket 查看求购市场");
            
            plugin.getLogger().info(player.getName() + " 创建了收购订单: " + itemName + " x" + amount + " 单价: " + unitPrice);
            return true;
        } else {
            // 订单创建失败，退款
            economyManager.deposit(player, totalPrice);
            player.sendMessage("§c创建收购订单失败！已退款");
            return false;
        }
    }
}