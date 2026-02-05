package org.playermarket.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.playermarket.PlayerMarket;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class I18n {
    private static final ConcurrentHashMap<Player, ResourceBundle> playerBundles = new ConcurrentHashMap<>();
    private static ResourceBundle defaultBundle;
    private static ResourceBundle fallbackBundle;
    private static PlayerMarket plugin;
    
    public static void initialize(PlayerMarket pluginInstance) {
        plugin = pluginInstance;
        saveDefaultLanguageFiles();
        loadDefaultBundle();
    }
    
    private static void saveDefaultLanguageFiles() {
        String[] langs = {"zh_CN", "en_US"};
        for (String lang : langs) {
            String fileName = "messages_" + lang + ".properties";
            File file = new File(plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                plugin.saveResource(fileName, false);
            }
        }
    }

    private static void loadDefaultBundle() {
        String defaultLang = plugin.getConfig().getString("language.default", "en_US");
        
        // 1. 尝试从文件加载自定义语言包
        defaultBundle = loadBundleFromFile(defaultLang);
        
        // 2. 始终加载JAR包内的语言包作为后备
        fallbackBundle = loadBundleFromJar(defaultLang);
        if (fallbackBundle == null) {
            // 如果指定语言的后备包加载失败，尝试加载默认英文后备包
            fallbackBundle = loadBundleFromJar("en_US");
        }

        // 如果文件加载失败，直接使用后备包作为主包
        if (defaultBundle == null) {
            defaultBundle = fallbackBundle;
        }
    }
    
    private static ResourceBundle loadBundleFromJar(String lang) {
        if (lang == null || lang.equalsIgnoreCase("auto")) {
            return null;
        }
        
        String fileName = "messages_" + lang + ".properties";
        try (java.io.InputStream is = plugin.getResource(fileName)) {
            if (is != null) {
                return new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load fallback language file from JAR: " + fileName);
        }
        return null;
    }

    private static ResourceBundle loadBundleFromFile(String lang) {
        if (lang == null || lang.equalsIgnoreCase("auto")) {
            return null;
        }
        
        String fileName = "messages_" + lang + ".properties";
        File file = new File(plugin.getDataFolder(), fileName);
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                return new PropertyResourceBundle(reader);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load language file: " + fileName);
                e.printStackTrace();
            }
        }
        return null;
    }
    
    private static Locale parseLocale(String lang) {
        if (lang != null && lang.equals("zh_CN")) {
            return new Locale("zh", "CN");
        } else if (lang != null && lang.equals("en_US")) {
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
        ResourceBundle bundle = loadBundleFromFile(lang);
        if (bundle != null) {
            playerBundles.put(player, bundle);
            return;
        }

        // 如果文件不存在，尝试从JAR加载
        bundle = loadBundleFromJar(lang);
        if (bundle != null) {
            playerBundles.put(player, bundle);
        } else {
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
        String message = key;
        
        try {
            if (bundle != null && bundle.containsKey(key)) {
                message = bundle.getString(key);
            } else if (fallbackBundle != null && fallbackBundle.containsKey(key)) {
                // 如果玩家特定的包（或默认包）中没有该键，尝试从后备包中获取
                message = fallbackBundle.getString(key);
            }
            
            if (args.length > 0) {
                return String.format(message, args);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }
    
    public static String get(String key, Object... args) {
        String message = key;
        try {
            if (defaultBundle != null && defaultBundle.containsKey(key)) {
                message = defaultBundle.getString(key);
            } else if (fallbackBundle != null && fallbackBundle.containsKey(key)) {
                message = fallbackBundle.getString(key);
            }
            
            if (args.length > 0) {
                return String.format(message, args);
            }
            return message;
        } catch (Exception e) {
            return key;
        }
    }
    
    public static void reload() {
        ResourceBundle.clearCache();
        loadDefaultBundle();
        // 清除玩家缓存，以便重新加载
        playerBundles.clear();
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