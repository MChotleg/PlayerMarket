package org.playermarket.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

public class UpdateChecker {
    private final JavaPlugin plugin;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void checkAsync() {
        String url = plugin.getConfig().getString("updates.check-url", "");
        boolean notifyConsole = plugin.getConfig().getBoolean("updates.notify-console", true);
        boolean notifyPlayers = plugin.getConfig().getBoolean("updates.notify-players", false);
        String permission = plugin.getConfig().getString("updates.permission", "playermarket.update");

        if (url == null || url.trim().isEmpty()) {
            if (notifyConsole) {
                plugin.getLogger().info(I18n.get("update.disabled"));
            }
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion;
                String latestUrl = url;

                URL u = new URL(url);
                HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
                conn.setRequestProperty("User-Agent", "PlayerMarket UpdateChecker");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String body = sb.toString().trim();

                if (body.startsWith("{") && body.endsWith("}")) {
                    String v = extractJson(body, "version");
                    String link = extractJson(body, "url");
                    latestVersion = v != null ? v.trim() : plugin.getDescription().getVersion();
                    if (link != null && !link.isEmpty()) latestUrl = link.trim();
                } else {
                    latestVersion = body;
                }

                String currentVersion = plugin.getDescription().getVersion();
                if (isUpdateAvailable(currentVersion, latestVersion)) {
                    if (notifyConsole) {
                        plugin.getLogger().info(I18n.get("update.available", currentVersion, latestVersion));
                        plugin.getLogger().info(I18n.get("update.url", latestUrl));
                    }
                    if (notifyPlayers) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission(permission)) {
                                p.sendMessage(I18n.get(p, "update.available", currentVersion, latestVersion));
                                p.sendMessage(I18n.get(p, "update.url", latestUrl));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning(I18n.get("update.check.failed", e.getMessage()));
            }
        });
    }

    private boolean isUpdateAvailable(String current, String latest) {
        if (current == null || latest == null) return false;
        return compareVersions(latest, current) > 0;
    }

    private int compareVersions(String a, String b) {
        String[] as = a.toLowerCase(Locale.ROOT).split("\\.");
        String[] bs = b.toLowerCase(Locale.ROOT).split("\\.");
        int n = Math.max(as.length, bs.length);
        for (int i = 0; i < n; i++) {
            int ai = i < as.length ? parseIntSafe(as[i]) : 0;
            int bi = i < bs.length ? parseIntSafe(bs[i]) : 0;
            if (ai != bi) return Integer.compare(ai, bi);
        }
        return 0;
    }

    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractJson(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"";
        int start = json.indexOf(pattern);
        if (start < 0) return null;
        start = start + pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return null;
        return json.substring(start, end);
    }
}
