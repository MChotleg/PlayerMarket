package org.playermarket.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.playermarket.PlayerMarket;

import java.util.ArrayList;
import java.util.List;

public class MarketCommand implements CommandExecutor, TabExecutor {
    private final PlayerMarket plugin;

    public MarketCommand(PlayerMarket plugin) {
        this.plugin = plugin;
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
            player.sendMessage("§c你没有权限使用玩家市场");
            return false;
        }

        // 处理子命令
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "test":
                case "debug":
                    return handleDebugCommand(player, args);
                case "balance":
                case "bal":
                    return handleBalanceCommand(player, args);
                case "reload":
                    return handleReloadCommand(player, args);
                case "help":
                    return handleHelpCommand(player, args);
                default:
                    // 默认打开市场
                    return openMarket(player);
            }
        }

        // 没有参数时打开市场
        return openMarket(player);
    }

    private boolean openMarket(Player player) {
        try {
            // 检查经济系统
            if (!plugin.getEconomyManager().isEconomyEnabled()) {
                player.sendMessage("§c经济系统不可用，无法打开市场");
                return false;
            }

            // 显示玩家余额
            double balance = plugin.getEconomyManager().getBalance(player);
            player.sendMessage("§a你的余额: " + plugin.getEconomyManager().format(balance));

            // 打开市场界面
            plugin.getMarketGUI().openMarketGUI(player);
            return true;
        } catch (Exception e) {
            player.sendMessage("§c打开市场时发生错误: " + e.getMessage());
            plugin.getLogger().severe("打开市场GUI失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleDebugCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage("§c你没有权限执行此命令");
            return false;
        }

        player.sendMessage("§6=== PlayerMarket 调试信息 ===");
        player.sendMessage("§e经济系统状态: " +
                (plugin.getEconomyManager().isEconomyEnabled() ? "§a已连接" : "§c未连接"));

        if (plugin.getEconomyManager().isEconomyEnabled()) {
            double balance = plugin.getEconomyManager().getBalance(player);
            player.sendMessage("§e你的余额: " + plugin.getEconomyManager().format(balance));
            player.sendMessage("§e经济提供者: " + plugin.getEconomyManager().getProviderName());
        }

        player.sendMessage("§e服务器线程: " +
                (Bukkit.isPrimaryThread() ? "§a主线程" : "§c异步线程"));
        return true;
    }

    private boolean handleBalanceCommand(Player player, String[] args) {
        double balance = plugin.getEconomyManager().getBalance(player);
        player.sendMessage("§a你的余额: " + plugin.getEconomyManager().format(balance));
        return true;
    }

    private boolean handleReloadCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage("§c你没有权限重新加载配置");
            return false;
        }

        plugin.reloadConfig();
        player.sendMessage("§a配置文件已重新加载");
        return true;
    }

    private boolean handleHelpCommand(Player player, String[] args) {
        player.sendMessage("§6=== PlayerMarket 命令帮助 ===");
        player.sendMessage("§a/playermarket §7- 打开玩家市场");
        player.sendMessage("§a/playermarket balance §7- 查看余额");
        if (player.hasPermission("playermarket.admin")) {
            player.sendMessage("§a/playermarket debug §7- 调试信息");
            player.sendMessage("§a/playermarket reload §7- 重新加载配置");
        }
        player.sendMessage("§a/playermarket help §7- 显示此帮助");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> commands = new ArrayList<>();

            commands.add("help");
            commands.add("balance");

            if (sender.hasPermission("playermarket.admin")) {
                commands.add("debug");
                commands.add("reload");
            }

            for (String cmd : commands) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        }

        return completions;
    }
}
