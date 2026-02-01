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
            player.sendMessage("§6=== 玩家市场 - 上架帮助 ===");
            player.sendMessage("");
            player.sendMessage("§e用法: §f/manuela <数量> <单价>");
            player.sendMessage("");
            player.sendMessage("§e参数说明:");
            player.sendMessage("§7<数量>  §f- 要上架的物品数量");
            player.sendMessage("§7<单价>  §f- 每个物品的价格");
            player.sendMessage("");
            player.sendMessage("§e示例:");
            player.sendMessage("§7/manuela 64 100  §f- 上架64个物品，每个100元");
            player.sendMessage("§7/manuela 10 50   §f- 上架10个物品，每个50元");
            player.sendMessage("");
            player.sendMessage("§e提示: §f手持要上架的物品后使用该命令");
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
        double totalPrice = amount * unitPrice;
        
        // 获取玩家手中的物品
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("§c你手中没有物品！请先手持要出售的物品");
            return false;
        }
        
        // 检查背包中是否有足够的物品
        int availableAmount = countItemInInventory(player, item);
        if (availableAmount < amount) {
            player.sendMessage("§c背包中物品数量不足！");
            player.sendMessage("§7需要: §e" + amount + " 个");
            player.sendMessage("§7拥有: §e" + availableAmount + " 个");
            player.sendMessage("§7物品: §e" + item.getType().name());
            return false;
        }
        
        // 创建市场商品（使用单价和数量）
        // 注意：itemBase64只存储单个物品（数量为1），避免显示数量角标
        ItemStack itemToSell = item.clone();
        itemToSell.setAmount(1);  // 设置为1，只存储样品
        
        MarketItem marketItem = new MarketItem(
            player.getUniqueId(),
            player.getName(),
            itemToSell,
            unitPrice,  // 单价
            amount,     // 实际数量
            totalPrice   // 总价
        );
        
        // 添加到数据库
        if (dbManager.addMarketItem(marketItem)) {
            // 从玩家背包中移除物品
            removeItemFromInventory(player, item, amount);
            
            // 获取物品名称（优先使用自定义名称）
            String itemName;
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemName = item.getItemMeta().getDisplayName();
            } else {
                itemName = item.getI18NDisplayName();
                if (itemName == null) {
                    itemName = item.getType().name();
                }
            }
            
            // 发送成功消息
            player.sendMessage("§a=== 物品上架成功 ===");
            player.sendMessage("§7商品ID: §e" + marketItem.getId());
            player.sendMessage("§7物品: §e" + itemName + " x" + amount);
            player.sendMessage("§7数量: §e" + amount);
            player.sendMessage("§7单价: §e" + economyManager.format(unitPrice));
            player.sendMessage("§7总价: §e" + economyManager.format(totalPrice));
            player.sendMessage("");
            player.sendMessage("§a提示: 使用 /playermarket 查看市场");
            
            plugin.getLogger().info(player.getName() + " 上架了物品: " + itemName + " x" + amount + " 单价: " + unitPrice);
            return true;
        } else {
            player.sendMessage("§c上架失败！请稍后重试");
            return false;
        }
    }
    
    // 计算背包中某种物品的数量
    private int countItemInInventory(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && invItem.isSimilar(item)) {
                count += invItem.getAmount();
            }
        }
        return count;
    }
    
    // 从背包中移除指定数量的物品
    private void removeItemFromInventory(Player player, ItemStack item, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack invItem = contents[i];
            if (invItem != null && invItem.isSimilar(item)) {
                int invAmount = invItem.getAmount();
                
                if (invAmount <= remaining) {
                    // 完全移除这个物品
                    player.getInventory().setItem(i, null);
                    remaining -= invAmount;
                } else {
                    // 部分移除
                    invItem.setAmount(invAmount - remaining);
                    remaining = 0;
                }
            }
        }
        
        player.updateInventory();
    }
}