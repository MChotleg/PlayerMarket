package org.playermarket.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class I18n {
    private static final ConcurrentHashMap<Player, ResourceBundle> playerBundles = new ConcurrentHashMap<>();
    private static ResourceBundle defaultBundle;
    private static PlayerMarket plugin;
    
    public static void initialize(PlayerMarket pluginInstance) {
        plugin = pluginInstance;
        loadDefaultBundle();
    }
    
    private static void loadDefaultBundle() {
        String defaultLang = plugin.getConfig().getString("language.default", "en_US");
        Locale locale = parseLocale(defaultLang);
        try {
            defaultBundle = ResourceBundle.getBundle("messages", locale);
        } catch (Exception e) {
            defaultBundle = ResourceBundle.getBundle("messages", new Locale("en", "US"));
        }
    }
    
    private static Locale parseLocale(String lang) {
        if (lang.equals("zh_CN")) {
            return new Locale("zh", "CN");
        } else if (lang.equals("en_US")) {
            return new Locale("en", "US");
        }
        return new Locale("en", "US");
    }
    
    private static Locale detectClientLocale(Player player) {
        try {
            // 尝试通过反射调用 getLocale() 方法，兼容不同版本
            Object spigot = player.spigot();
            java.lang.reflect.Method getLocaleMethod = spigot.getClass().getMethod("getLocale");
            String clientLocale = (String) getLocaleMethod.invoke(spigot);
            if (clientLocale != null) {
                if (clientLocale.startsWith("zh")) {
                    return new Locale("zh", "CN");
                } else if (clientLocale.startsWith("en")) {
                    return new Locale("en", "US");
                }
            }
        } catch (Exception e) {
            // 如果方法不存在或调用失败，默认使用英语
        }
        return new Locale("en", "US");
    }
    
    public static void setPlayerLanguage(Player player, String lang) {
        Locale locale = parseLocale(lang);
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
            playerBundles.put(player, bundle);
        } catch (Exception e) {
            playerBundles.remove(player);
        }
    }
    
    public static void resetPlayerLanguage(Player player) {
        playerBundles.remove(player);
    }
    
    public static boolean hasCustomLanguage(Player player) {
        return playerBundles.containsKey(player);
    }
    
    private static ResourceBundle getPlayerBundle(Player player) {
        // 如果玩家设置了自定义语言，使用自定义设置
        if (playerBundles.containsKey(player)) {
            return playerBundles.get(player);
        }
        
        // 否则使用服务器默认语言（auto模式）
        return defaultBundle;
    }
    
    public static String get(Player player, String key, Object... args) {
        if (player == null) {
            return get(key, args);
        }
        ResourceBundle bundle = getPlayerBundle(player);
        try {
            String message = bundle.getString(key);
            if (args.length > 0) {
                return String.format(message, args);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }
    
    public static String get(String key, Object... args) {
        try {
            String message = defaultBundle.getString(key);
            if (args.length > 0) {
                return String.format(message, args);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }
    
    public static void reload() {
        loadDefaultBundle();
        // 不清除playerBundles，玩家自定义语言设置应保留
    }
    
    public static String getItemDisplayName(ItemStack itemStack) {
        if (itemStack == null) {
            return "未知物品";
        }
        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String displayName = meta.getDisplayName();
                return displayName;
            }
        }
        String i18nName = itemStack.getI18NDisplayName();
        String result = i18nName != null ? i18nName : itemStack.getType().name();
        return result;
    }
    
    public static String stripColorCodes(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("§.", "");
    }
}