package org.playermarket.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.playermarket.PlayerMarket;
import org.playermarket.utils.I18n;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EconomyManager {
    private final PlayerMarket plugin;
    private Economy economy;
    private boolean enabled = false;
    private String providerName = "未知";
    private boolean currencyFormatEnabled = true;
    private boolean detectSymbol = true;
    private String defaultSymbol = "⛃";
    private String symbolPosition = "prefix"; // prefix or suffix
    private int decimalPlaces = -1; // -1 means follow provider
    private boolean thousandsSeparator = true;
    private boolean alwaysShowDecimals = false;
    private final Map<String, String> providerSymbols = new HashMap<>();

    public EconomyManager(PlayerMarket plugin) {
        this.plugin = plugin;
        setupEconomy();
        loadCurrencyConfig();
    }

    private boolean setupEconomy() {
        try {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                plugin.getLogger().warning(I18n.get("economy.vault.notfound"));
                return false;
            }

            // 获取配置的经济提供者名称
            String configuredProvider = plugin.getConfig().getString("economy.provider", "default");
            Collection<RegisteredServiceProvider<Economy>> providers = plugin.getServer()
                    .getServicesManager().getRegistrations(Economy.class);
            
            if (providers == null || providers.isEmpty()) {
                plugin.getLogger().warning(I18n.get("economy.provider.notfound"));
                return false;
            }

            RegisteredServiceProvider<Economy> selectedProvider = null;
            
            if (configuredProvider.equalsIgnoreCase("default")) {
                // 使用第一个可用的提供者
                selectedProvider = providers.iterator().next();
                plugin.getLogger().info(I18n.get("economy.connected.default", selectedProvider.getProvider().getName()));
            } else {
                // 查找匹配名称的提供者
                for (RegisteredServiceProvider<Economy> provider : providers) {
                    if (provider.getProvider().getName().equalsIgnoreCase(configuredProvider)) {
                        selectedProvider = provider;
                        break;
                    }
                }
                
                if (selectedProvider == null) {
                    // 未找到配置的提供者，使用第一个并记录警告
                    selectedProvider = providers.iterator().next();
                    plugin.getLogger().warning(I18n.get("economy.provider.notfound.configured", configuredProvider, selectedProvider.getProvider().getName()));
                } else {
                    plugin.getLogger().info(I18n.get("economy.connected.configured", configuredProvider));
                }
            }

            economy = selectedProvider.getProvider();
            providerName = economy.getName();
            enabled = true;
            plugin.getLogger().info(I18n.get("economy.connected", providerName));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe(I18n.get("economy.init.failed", e.getMessage()));
            return false;
        }
    }

    private void loadCurrencyConfig() {
        try {
            currencyFormatEnabled = plugin.getConfig().getBoolean("economy.currency.enabled", true);
            detectSymbol = plugin.getConfig().getBoolean("economy.currency.detect-symbol", true);
            defaultSymbol = plugin.getConfig().getString("economy.currency.default-symbol", "⛃");
            symbolPosition = plugin.getConfig().getString("economy.currency.position", "prefix");
            decimalPlaces = plugin.getConfig().getInt("economy.currency.decimal-places", -1);
            thousandsSeparator = plugin.getConfig().getBoolean("economy.currency.thousands-separator", true);
            alwaysShowDecimals = plugin.getConfig().getBoolean("economy.currency.always-show-decimals", false);
            // provider-symbols
            Map<String, Object> map = plugin.getConfig().getConfigurationSection("economy.currency.provider-symbols") != null
                    ? plugin.getConfig().getConfigurationSection("economy.currency.provider-symbols").getValues(false)
                    : null;
            if (map != null) {
                providerSymbols.clear();
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    if (e.getValue() != null) {
                        providerSymbols.put(e.getKey(), String.valueOf(e.getValue()));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    // 检查经济系统是否可用
    public boolean isEconomyEnabled() {
        return enabled && economy != null;
    }

    // 获取经济提供者名称
    public String getProviderName() {
        return providerName;
    }

    public double getBalance(Player player) {
        if (!isEconomyEnabled()) return 0.0;
        try {
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.getbalance.failed", e.getMessage()));
            return 0.0;
        }
    }
    
    public double getBalance(java.util.UUID playerUuid) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return 0.0;
        return getBalance(player);
    }

    public boolean deposit(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            economy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.deposit.failed", e.getMessage()));
            return false;
        }
    }
    
    public boolean deposit(java.util.UUID playerUuid, double amount) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return false;
        return deposit(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        if (amount <= 0) return false;

        try {
            if (economy.has(player, amount)) {
                economy.withdrawPlayer(player, amount);
                return true;
            }
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.withdraw.failed", e.getMessage()));
            return false;
        }
    }
    
    public boolean withdraw(java.util.UUID playerUuid, double amount) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return false;
        return withdraw(player, amount);
    }

    public String format(double amount) {
        if (!isEconomyEnabled()) return String.valueOf(amount);
        try {
            if (!currencyFormatEnabled) {
                return economy.format(amount);
            }
            String symbol = resolveSymbol();
            if (decimalPlaces < 0) {
                // 跟随提供者格式
                String base = economy.format(amount);
                // 若配置要求替换符号，则尝试替换或追加
                if (symbol != null && !symbol.isEmpty()) {
                    String numeric = base.replaceAll("[^0-9.,]", "").trim();
                    if (numeric.isEmpty()) {
                        numeric = String.valueOf(amount);
                    }
                    return symbolPosition.equalsIgnoreCase("suffix")
                            ? numeric + " " + symbol
                            : symbol + numeric;
                }
                return base;
            } else {
                // 使用自定义数字格式
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                symbols.setGroupingSeparator(thousandsSeparator ? ',' : '\0');
                symbols.setDecimalSeparator('.');
                StringBuilder pattern = new StringBuilder();
                pattern.append("#");
                if (thousandsSeparator) pattern.insert(0, "#,###");
                if (decimalPlaces > 0 || alwaysShowDecimals) {
                    pattern.append(".");
                    for (int i = 0; i < decimalPlaces; i++) {
                        pattern.append(alwaysShowDecimals ? "0" : "#");
                    }
                }
                DecimalFormat df = new DecimalFormat(pattern.toString(), symbols);
                String number = df.format(amount);
                if (symbol != null && !symbol.isEmpty()) {
                    return symbolPosition.equalsIgnoreCase("suffix")
                            ? number + " " + symbol
                            : symbol + number;
                }
                return number;
            }
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.format.failed", e.getMessage()));
            return String.valueOf(amount);
        }
    }

    public boolean hasEnough(Player player, double amount) {
        if (!isEconomyEnabled()) return false;
        try {
            return economy.has(player, amount);
        } catch (Exception e) {
            plugin.getLogger().warning(I18n.get("economy.has.failed", e.getMessage()));
            return false;
        }
    }
    
    public boolean hasEnough(java.util.UUID playerUuid, double amount) {
        Player player = Bukkit.getPlayer(playerUuid);
        if (player == null) return false;
        return hasEnough(player, amount);
    }

    // 获取经济实例（高级用法）
    public Economy getEconomy() {
        return economy;
    }

    public String getCurrencySymbol() {
        return resolveSymbol();
    }

    private String resolveSymbol() {
        String mapped = providerSymbols.getOrDefault(providerName, null);
        if (mapped != null && !mapped.isEmpty()) return mapped;
        if (detectSymbol) {
            try {
                String sample = economy.format(1234.56);
                String prefix = sample.replaceAll("[0-9.,\\s]+", "").trim();
                if (!prefix.isEmpty()) return prefix;
                String digits = sample.replaceAll("[^0-9.,]", "").trim();
                String suffix = sample.replace(digits, "").trim();
                if (!suffix.isEmpty()) return suffix;
            } catch (Exception ignored) {
            }
        }
        return defaultSymbol;
    }
}
