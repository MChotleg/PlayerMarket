package org.playermarket.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;

import java.util.List;

/**
 * 处理玩家加入事件的监听器
 */
public class PlayerJoinListener extends BaseMarketListener implements Listener {
    
    public PlayerJoinListener(PlayerMarket plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 调试日志：检查更新提示逻辑
        String newVersion = plugin.getNewVersion();
        boolean hasPerm = player.hasPermission("playermarket.update");
        String currentVersion = plugin.getDescription().getVersion();
        
        plugin.getLogger().info("[Debug] PlayerJoin - Name: " + player.getName());
        plugin.getLogger().info("[Debug] PlayerJoin - CurrentVer: " + currentVersion);
        plugin.getLogger().info("[Debug] PlayerJoin - NewVerCached: " + newVersion);
        plugin.getLogger().info("[Debug] PlayerJoin - HasPerm: " + hasPerm);

        if (newVersion != null && hasPerm) {
            player.sendMessage(I18n.get(player, "update.available", currentVersion, newVersion));
            String url = plugin.getUpdateUrl();
            if (url != null && !url.isEmpty()) {
                player.sendMessage(I18n.get(player, "update.url", url));
            }
        }
        
        // 异步获取并显示未读通知
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> notifications = dbManager.getUnreadNotifications(player.getUniqueId());
            
            if (!notifications.isEmpty()) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                    player.sendMessage("");
                    player.sendMessage(I18n.get(player, "marketlistener.notification_title"));
                    player.sendMessage("");
                    for (String notification : notifications) {
                        player.sendMessage(notification);
                    }
                    player.sendMessage("");
                    player.sendMessage(I18n.get(player, "marketlistener.notification_count", notifications.size()));
                    player.sendMessage("");
                    
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        dbManager.markNotificationsAsRead(player.getUniqueId());
                    });
                });
            }
        });
    }
}