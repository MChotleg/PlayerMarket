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