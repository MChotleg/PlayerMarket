package org.playermarket.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarketCommand implements CommandExecutor, TabExecutor {
    private final PlayerMarket plugin;

    public MarketCommand(PlayerMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查是否为玩家
        if (!(sender instanceof Player)) {
            sender.sendMessage(I18n.get("error.player_only"));
            return false;
        }

        Player player = (Player) sender;

        // 检查权限
        if (!player.hasPermission("playermarket.use")) {
            player.sendMessage(I18n.get(player, "error.permission"));
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
                case "lang":
                case "language":
                    return handleLanguageCommand(player, args);
                case "defaultlang":
                case "defaultlanguage":
                    return handleDefaultLanguageCommand(player, args);
                case "audit":
                    return handleAuditCommand(player, args);
                case "featured":
                    return handleFeaturedCommand(player, args);
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
                player.sendMessage(I18n.get(player, "error.economy"));
                return false;
            }

            // 打开市场界面
            plugin.getMarketGUI().openMarketGUI(player);
            return true;
        } catch (Exception e) {
            player.sendMessage(I18n.get(player, "error.market_open", e.getMessage()));
            plugin.getLogger().severe("打开市场GUI失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean handleDebugCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }

        player.sendMessage(I18n.get(player, "debug.title"));
        player.sendMessage(I18n.get(player, "debug.economy_status") +
                (plugin.getEconomyManager().isEconomyEnabled() ? I18n.get(player, "debug.connected") : I18n.get(player, "debug.disconnected")));

        if (plugin.getEconomyManager().isEconomyEnabled()) {
            double balance = plugin.getEconomyManager().getBalance(player);
            player.sendMessage(I18n.get(player, "debug.balance", plugin.getEconomyManager().format(balance)));
            player.sendMessage(I18n.get(player, "debug.provider", plugin.getEconomyManager().getProviderName()));
        }

        player.sendMessage(I18n.get(player, "debug.thread") + (Bukkit.isPrimaryThread() ? I18n.get(player, "debug.main_thread") : I18n.get(player, "debug.async_thread")));
        return true;
    }

    private boolean handleBalanceCommand(Player player, String[] args) {
        double balance = plugin.getEconomyManager().getBalance(player);
        player.sendMessage(I18n.get(player, "command.balance", plugin.getEconomyManager().format(balance)));
        return true;
    }

    private boolean handleReloadCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }

        plugin.reloadConfig();
        I18n.reload();
        player.sendMessage(I18n.get(player, "command.reload"));
        return true;
    }

    private boolean handleHelpCommand(Player player, String[] args) {
        player.sendMessage(I18n.get(player, "command.help.title"));
        player.sendMessage(I18n.get(player, "command.help.open"));
        player.sendMessage(I18n.get(player, "command.help.balance"));
        player.sendMessage(I18n.get(player, "command.help.lang"));
        player.sendMessage(I18n.get(player, "command.help.help"));
        
        if (player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "command.help.debug"));
            player.sendMessage(I18n.get(player, "command.help.reload"));
            player.sendMessage(I18n.get(player, "command.help.defaultlang"));
            player.sendMessage(I18n.get(player, "command.help.audit"));
            player.sendMessage(I18n.get(player, "command.help.featured"));
        }
        return true;
    }

    private boolean handleLanguageCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(I18n.get(player, "error.lang.usage"));
            return false;
        }

        String lang = args[1].toLowerCase();
        if (lang.equals("zh_cn") || lang.equals("en_us")) {
            String normalizedLang = lang.equals("zh_cn") ? "zh_CN" : "en_US";
            I18n.setPlayerLanguage(player, normalizedLang);
            player.sendMessage("§a你的语言已设置为: " + (normalizedLang.equals("zh_CN") ? "中文" : "English"));
        } else if (lang.equals("auto")) {
            // 重置为默认模式（使用服务器默认语言）
            I18n.resetPlayerLanguage(player);
            String defaultLang = plugin.getConfig().getString("language.default", "zh_CN");
            String langDisplay = defaultLang.equals("zh_CN") ? "中文" : "English";
            player.sendMessage("§a语言已设置为: 自动（使用服务器默认语言: " + langDisplay + "）");
        } else {
            player.sendMessage(I18n.get(player, "error.lang.invalid"));
        }
        return true;
    }

    private boolean handleDefaultLanguageCommand(Player player, String[] args) {
        // 检查管理员权限
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }

        if (args.length < 2) {
            player.sendMessage(I18n.get(player, "error.defaultlang.usage"));
            return false;
        }

        String lang = args[1].toLowerCase();
        if (lang.equals("zh_cn") || lang.equals("en_us")) {
            String normalizedLang = lang.equals("zh_cn") ? "zh_CN" : "en_US";
            plugin.getConfig().set("language.default", normalizedLang);
            plugin.saveConfig();
            I18n.reload();
            player.sendMessage("§a服务器默认语言已设置为: " + (normalizedLang.equals("zh_CN") ? "中文" : "English"));
        } else {
            player.sendMessage(I18n.get(player, "error.defaultlang.invalid"));
        }
        return true;
    }

    private boolean handleAuditCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }

        double minAmount = 1000.0;
        int minCount = 5;

        if (args.length > 1) {
            try {
                minAmount = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(I18n.get(player, "command.audit.usage"));
                return false;
            }
        }

        if (args.length > 2) {
            try {
                minCount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(I18n.get(player, "command.audit.usage"));
                return false;
            }
        }

        final double finalMinAmount = minAmount;
        final int finalMinCount = minCount;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Map<String, Object>> suspicious = plugin.getDatabaseManager().getSuspiciousTransactions(finalMinAmount, finalMinCount);
            
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(I18n.get(player, "command.audit.header", finalMinAmount, finalMinCount));
                
                if (suspicious.isEmpty()) {
                    player.sendMessage(I18n.get(player, "command.audit.empty"));
                    return;
                }
                
                for (Map<String, Object> record : suspicious) {
                    UUID buyerUuid = (UUID) record.get("buyer_uuid");
                    UUID sellerUuid = (UUID) record.get("seller_uuid");
                    String buyerName = Bukkit.getOfflinePlayer(buyerUuid).getName();
                    String sellerName = Bukkit.getOfflinePlayer(sellerUuid).getName();
                    
                    if (buyerName == null) buyerName = buyerUuid.toString().substring(0, 8);
                    if (sellerName == null) sellerName = sellerUuid.toString().substring(0, 8);
                    
                    player.sendMessage(I18n.get(player, "command.audit.format", 
                        buyerName, 
                        sellerName, 
                        record.get("trade_count"), 
                        String.format("%.2f", record.get("total_amount")),
                        record.get("ips") != null ? ((String)record.get("ips")).split(",").length : 0
                    ));
                    
                    if (record.get("ips") != null) {
                         player.sendMessage("    §7IPs: " + record.get("ips"));
                    }
                }
            });
        });

        return true;
    }

    private boolean handleFeaturedCommand(Player player, String[] args) {
        if (!player.hasPermission("playermarket.admin")) {
            player.sendMessage(I18n.get(player, "error.permission"));
            return false;
        }

        if (args.length < 3) {
            player.sendMessage(I18n.get(player, "command.featured.usage"));
            return false;
        }

        String action = args[1].toLowerCase();
        int slot;
        try {
            slot = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(I18n.get(player, "command.featured.invalid_slot"));
            return false;
        }

        if (slot < 1 || slot > 14) {
            player.sendMessage(I18n.get(player, "command.featured.invalid_slot"));
            return false;
        }

        int index = slot - 1;
        List<String> featuredShops = plugin.getConfig().getStringList("player-shops.featured-shops");
        if (featuredShops == null) {
            featuredShops = new ArrayList<>();
        }

        // 确保列表足够长
        while (featuredShops.size() <= index) {
            featuredShops.add("null");
        }

        if (action.equals("set")) {
            if (args.length < 4) {
                player.sendMessage(I18n.get(player, "command.featured.usage"));
                return false;
            }
            String playerName = args[3];
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(I18n.get(player, "command.featured.player_not_found", playerName));
                return false;
            }

            featuredShops.set(index, target.getUniqueId().toString());
            plugin.getConfig().set("player-shops.featured-shops", featuredShops);
            plugin.saveConfig();
            player.sendMessage(I18n.get(player, "command.featured.set_success", slot, target.getName()));

        } else if (action.equals("remove")) {
            featuredShops.set(index, "null");
            plugin.getConfig().set("player-shops.featured-shops", featuredShops);
            plugin.saveConfig();
            player.sendMessage(I18n.get(player, "command.featured.remove_success", slot));
        } else {
            player.sendMessage(I18n.get(player, "command.featured.usage"));
            return false;
        }

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
            commands.add("lang");
            commands.add("language");

            if (sender.hasPermission("playermarket.admin")) {
                commands.add("debug");
                commands.add("reload");
                commands.add("defaultlang");
                commands.add("defaultlanguage");
                commands.add("audit");
                commands.add("featured");
            }

            for (String cmd : commands) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        } else {
            // 处理子命令参数补全
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("lang") || subCommand.equals("language")) {
                if (args.length == 2) {
                    List<String> languages = new ArrayList<>();
                    languages.add("zh_CN");
                    languages.add("en_US");
                    languages.add("auto");
                    String partial = args[1].toLowerCase();
                    for (String lang : languages) {
                        if (lang.toLowerCase().startsWith(partial)) {
                            completions.add(lang);
                        }
                    }
                }
            } else if ((subCommand.equals("defaultlang") || subCommand.equals("defaultlanguage")) && sender.hasPermission("playermarket.admin")) {
                if (args.length == 2) {
                    List<String> languages = new ArrayList<>();
                    languages.add("zh_CN");
                    languages.add("en_US");
                    String partial = args[1].toLowerCase();
                    for (String lang : languages) {
                        if (lang.toLowerCase().startsWith(partial)) {
                            completions.add(lang);
                        }
                    }
                }
            } else if (subCommand.equals("featured") && sender.hasPermission("playermarket.admin")) {
                if (args.length == 2) {
                    String partial = args[1].toLowerCase();
                    if ("set".startsWith(partial)) completions.add("set");
                    if ("remove".startsWith(partial)) completions.add("remove");
                } else if (args.length == 3) {
                    String partial = args[2].toLowerCase();
                    for (int i = 1; i <= 14; i++) {
                        if (String.valueOf(i).startsWith(partial)) {
                            completions.add(String.valueOf(i));
                        }
                    }
                } else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
                    return null; // Return null to show player list
                }
            }
        }

        return completions;
    }
}
