package org.playermarket.database;

import org.playermarket.model.MarketItem;
import org.playermarket.model.BuyOrder;
import org.playermarket.PlayerMarket;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    private final PlayerMarket plugin;
    private Connection connection;
    
    public DatabaseManager(PlayerMarket plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try {
            // 确保插件数据文件夹存在
            plugin.getDataFolder().mkdirs();
            
            // SQLite连接URL
            String url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/market.db";
            
            // 创建连接
            connection = DriverManager.getConnection(url);
            
            // 创建表
            createTables();
            
            plugin.getLogger().info("数据库连接已建立");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "数据库初始化失败", e);
        }
    }
    
    private void createTables() throws SQLException {
        String createMarketItemsTable = "CREATE TABLE IF NOT EXISTS market_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "seller_uuid TEXT NOT NULL," +
                "seller_name TEXT NOT NULL," +
                "item_base64 TEXT NOT NULL," +
                "amount INTEGER DEFAULT 1," +
                "unit_price REAL NOT NULL," +
                "price REAL NOT NULL," +
                "list_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "sold BOOLEAN DEFAULT FALSE," +
                "buyer_uuid TEXT," +
                "buyer_name TEXT," +
                "sold_time TIMESTAMP," +
                "CHECK (price >= 0)" +
                ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createMarketItemsTable);
            plugin.getLogger().info("数据表已创建/验证");
        }
        
        // 创建仓库表
        String createWarehouseItemsTable = "CREATE TABLE IF NOT EXISTS warehouse_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner_uuid TEXT NOT NULL," +
                "owner_name TEXT NOT NULL," +
                "item_base64 TEXT NOT NULL," +
                "amount INTEGER DEFAULT 1," +
                "original_price REAL NOT NULL," +
                "original_unit_price REAL NOT NULL," +
                "delisting_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "original_market_item_id INTEGER" +
                ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createWarehouseItemsTable);
            plugin.getLogger().info("仓库数据表已创建/验证");
        }
        
        // 创建通知表
        String createNotificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "player_uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "notification_type TEXT NOT NULL," +
                "message TEXT NOT NULL," +
                "data TEXT," +
                "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "read BOOLEAN DEFAULT FALSE" +
                ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createNotificationsTable);
            plugin.getLogger().info("通知数据表已创建/验证");
        }
        
        // 创建收购订单表
        String createBuyOrdersTable = "CREATE TABLE IF NOT EXISTS buy_orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "buyer_uuid TEXT NOT NULL," +
                "buyer_name TEXT NOT NULL," +
                "item_base64 TEXT NOT NULL," +
                "amount INTEGER DEFAULT 1," +
                "remaining_amount INTEGER DEFAULT 1," +
                "unit_price REAL NOT NULL," +
                "total_price REAL NOT NULL," +
                "remaining_total_price REAL NOT NULL," +
                "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "fulfilled BOOLEAN DEFAULT FALSE," +
                "seller_uuid TEXT," +
                "seller_name TEXT," +
                "fulfill_time TIMESTAMP," +
                "CHECK (total_price >= 0)" +
                ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBuyOrdersTable);
            plugin.getLogger().info("收购订单数据表已创建/验证");
        }
        
        // 数据库迁移：检查并添加缺失的列
        migrateDatabase();
    }
    
    private void migrateDatabase() throws SQLException {
        // 检查并添加 amount 列
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "market_items", "amount");
            if (!columns.next()) {
                plugin.getLogger().info("检测到旧版本数据库，正在添加 amount 列...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE market_items ADD COLUMN amount INTEGER DEFAULT 1");
                    plugin.getLogger().info("amount 列添加成功");
                }
            }
            columns.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("数据库迁移失败: " + e.getMessage());
        }
        
        // 检查并添加 unit_price 列
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "market_items", "unit_price");
            if (!columns.next()) {
                plugin.getLogger().info("检测到旧版本数据库，正在添加 unit_price 列...");
                try (Statement stmt = connection.createStatement()) {
                    // 先添加 unit_price 列
                    stmt.execute("ALTER TABLE market_items ADD COLUMN unit_price REAL");
                    plugin.getLogger().info("unit_price 列添加成功");
                    // 为现有数据计算单价
                    int updatedRows = stmt.executeUpdate("UPDATE market_items SET unit_price = price / amount WHERE amount > 0 AND unit_price IS NULL");
                    plugin.getLogger().info("已更新 " + updatedRows + " 条数据的单价");
                }
            }
            columns.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("数据库迁移失败: " + e.getMessage());
        }

        // buy_orders: remaining_amount
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "buy_orders", "remaining_amount");
            if (!columns.next()) {
                plugin.getLogger().info("检测到旧版本数据库，正在添加 buy_orders.remaining_amount 列...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE buy_orders ADD COLUMN remaining_amount INTEGER");
                    stmt.executeUpdate("UPDATE buy_orders SET remaining_amount = amount WHERE remaining_amount IS NULL");
                    plugin.getLogger().info("buy_orders.remaining_amount 列添加成功");
                }
            }
            columns.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("数据库迁移失败: " + e.getMessage());
        }

        // buy_orders: remaining_total_price
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "buy_orders", "remaining_total_price");
            if (!columns.next()) {
                plugin.getLogger().info("检测到旧版本数据库，正在添加 buy_orders.remaining_total_price 列...");
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE buy_orders ADD COLUMN remaining_total_price REAL");
                    stmt.executeUpdate("UPDATE buy_orders SET remaining_total_price = total_price WHERE remaining_total_price IS NULL");
                    plugin.getLogger().info("buy_orders.remaining_total_price 列添加成功");
                }
            }
            columns.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("数据库迁移失败: " + e.getMessage());
        }
    }
    
    // 添加商品到市场（支持合并）
    public boolean addMarketItem(MarketItem item) {
        // 先检查是否存在相同卖家、相同物品、相同单价的商品
        MarketItem existingItem = findExistingItem(item.getSellerUuid(), item.getItemBase64(), item.getUnitPrice());
        
        if (existingItem != null && !existingItem.isSold()) {
            // 合并商品：更新数量和总价
            return mergeMarketItem(existingItem.getId(), item);
        } else {
            // 插入新商品
            return insertMarketItem(item);
        }
    }
    
    // 查找已存在的商品（用于合并）
    private MarketItem findExistingItem(UUID sellerUuid, String itemBase64, double unitPrice) {
        // 使用整数比较避免浮点数精度问题（乘以100后比较）
        long unitPriceInt = Math.round(unitPrice * 100);
        
        String sql = "SELECT * FROM market_items WHERE seller_uuid = ? AND item_base64 = ? AND sold = FALSE ORDER BY list_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, sellerUuid.toString());
            pstmt.setString(2, itemBase64);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MarketItem item = resultSetToMarketItem(rs);
                    // 使用整数比较避免浮点数精度问题
                    long existingUnitPriceInt = Math.round(item.getUnitPrice() * 100);
                    if (existingUnitPriceInt == unitPriceInt) {
                        return item;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "查找已存在商品失败", e);
        }
        
        return null;
    }
    
    // 合并商品（累加数量）
    private boolean mergeMarketItem(int existingItemId, MarketItem newItem) {
        // 先获取现有商品信息
        MarketItem existingItem = getItemById(existingItemId);
        if (existingItem == null) {
            return false;
        }
        
        // 计算新数量
        int newAmount = existingItem.getAmount() + newItem.getItemStack().getAmount();
        
        // 计算新总价（使用单价 * 新数量）
        double newPrice = existingItem.getUnitPrice() * newAmount;
        
        String sql = "UPDATE market_items SET " +
                     "amount = ?, " +
                     "price = ?, " +
                     "list_time = CURRENT_TIMESTAMP " +
                     "WHERE id = ? AND sold = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newAmount);
            pstmt.setDouble(2, newPrice);
            pstmt.setInt(3, existingItemId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取更新后的商品信息
                MarketItem mergedItem = getItemById(existingItemId);
                if (mergedItem != null) {
                    newItem.setId(mergedItem.getId());
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "合并商品失败", e);
        }
        return false;
    }
    
    // 插入新商品
    private boolean insertMarketItem(MarketItem item) {
        String sql = "INSERT INTO market_items (seller_uuid, seller_name, item_base64, amount, unit_price, price, list_time, sold) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getSellerUuid().toString());
            pstmt.setString(2, item.getSellerName());
            pstmt.setString(3, item.getItemBase64());
            pstmt.setInt(4, item.getItemStack().getAmount());
            pstmt.setDouble(5, item.getUnitPrice());
            pstmt.setDouble(6, item.getPrice());
            pstmt.setTimestamp(7, item.getListTime());
            pstmt.setBoolean(8, item.isSold());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取生成的ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "添加商品到数据库失败", e);
        }
        return false;
    }
    
    // 获取所有未售出的商品（分页）
    public List<MarketItem> getAvailableItems(int page, int pageSize) {
        List<MarketItem> items = new ArrayList<>();
        String sql = "SELECT * FROM market_items WHERE sold = FALSE ORDER BY list_time DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, (page - 1) * pageSize);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(resultSetToMarketItem(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取商品列表失败", e);
        }
        
        return items;
    }
    
    // 获取所有未售出的商品（不分页）
    public List<MarketItem> getAllUnsoldItems() {
        List<MarketItem> items = new ArrayList<>();
        String sql = "SELECT * FROM market_items WHERE sold = FALSE ORDER BY list_time DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(resultSetToMarketItem(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取商品列表失败", e);
        }
        
        return items;
    }
    
    // 获取玩家上架的商品
    public List<MarketItem> getPlayerItems(UUID playerUuid) {
        List<MarketItem> items = new ArrayList<>();
        String sql = "SELECT * FROM market_items WHERE seller_uuid = ? ORDER BY list_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(resultSetToMarketItem(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取玩家商品失败", e);
        }
        
        return items;
    }
    
    // 获取商品总数（用于分页）
    public int getTotalAvailableItems() {
        String sql = "SELECT COUNT(*) FROM market_items WHERE sold = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取商品总数失败", e);
        }
        return 0;
    }
    
    // 购买商品（支持部分购买）
    public boolean purchaseItem(int itemId, UUID buyerUuid, String buyerName, int purchaseAmount) {
        if (purchaseAmount <= 0) {
            return false;
        }

        try {
            String sqlFull = "UPDATE market_items SET sold = TRUE, buyer_uuid = ?, buyer_name = ?, sold_time = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND sold = FALSE AND amount = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlFull)) {
                pstmt.setString(1, buyerUuid.toString());
                pstmt.setString(2, buyerName);
                pstmt.setInt(3, itemId);
                pstmt.setInt(4, purchaseAmount);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return true;
                }
            }

            String sqlPartial = "UPDATE market_items SET amount = amount - ?, price = unit_price * (amount - ?) " +
                    "WHERE id = ? AND sold = FALSE AND amount >= ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlPartial)) {
                pstmt.setInt(1, purchaseAmount);
                pstmt.setInt(2, purchaseAmount);
                pstmt.setInt(3, itemId);
                pstmt.setInt(4, purchaseAmount);

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "购买商品失败", e);
        }
        return false;
    }
    
    // 根据ID获取商品
    public MarketItem getItemById(int itemId) {
        String sql = "SELECT * FROM market_items WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToMarketItem(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "根据ID获取商品失败", e);
        }
        return null;
    }
    
    // 移除商品（卖家取消上架）
    public boolean removeItem(int itemId, UUID sellerUuid) {
        String sql = "DELETE FROM market_items WHERE id = ? AND seller_uuid = ? AND sold = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setString(2, sellerUuid.toString());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "移除商品失败", e);
        }
        return false;
    }
    
    // 获取玩家上架的商品（分页）
    public List<MarketItem> getPlayerListings(UUID playerUuid, int page, int pageSize) {
        List<MarketItem> items = new ArrayList<>();
        String sql = "SELECT * FROM market_items WHERE seller_uuid = ? AND sold = FALSE ORDER BY list_time DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(resultSetToMarketItem(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取玩家商品失败", e);
        }
        
        return items;
    }
    
    // 获取玩家上架商品数量
    public int getPlayerListingsCount(UUID playerUuid) {
        String sql = "SELECT COUNT(*) FROM market_items WHERE seller_uuid = ? AND sold = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取玩家商品数量失败", e);
        }
        return 0;
    }
    
    public boolean partialDelistItem(int itemId, UUID sellerUuid, int delistAmount) {
        if (delistAmount <= 0) {
            return false;
        }

        try {
            String sqlFull = "DELETE FROM market_items WHERE id = ? AND seller_uuid = ? AND sold = FALSE AND amount = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlFull)) {
                pstmt.setInt(1, itemId);
                pstmt.setString(2, sellerUuid.toString());
                pstmt.setInt(3, delistAmount);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return true;
                }
            }

            String sqlPartial = "UPDATE market_items SET amount = amount - ?, price = unit_price * (amount - ?) " +
                    "WHERE id = ? AND seller_uuid = ? AND sold = FALSE AND amount >= ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sqlPartial)) {
                pstmt.setInt(1, delistAmount);
                pstmt.setInt(2, delistAmount);
                pstmt.setInt(3, itemId);
                pstmt.setString(4, sellerUuid.toString());
                pstmt.setInt(5, delistAmount);

                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "部分下架商品失败", e);
        }
        return false;
    }
    
    // 修改商品单价
    public boolean updateItemUnitPrice(int itemId, UUID sellerUuid, double newUnitPrice) {
        if (newUnitPrice <= 0) {
            return false;
        }
        
        try {
            // 使用原子更新，直接计算新价格
            String sql = "UPDATE market_items SET unit_price = ?, price = unit_price * amount * (1.0 / unit_price) * ? WHERE id = ? AND seller_uuid = ? AND sold = FALSE";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setDouble(1, newUnitPrice);
                pstmt.setDouble(2, newUnitPrice);
                pstmt.setInt(3, itemId);
                pstmt.setString(4, sellerUuid.toString());
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "修改商品单价失败", e);
        }
        return false;
    }
    
    // 添加物品到仓库（支持合并）
    public boolean addWarehouseItem(org.playermarket.model.WarehouseItem warehouseItem) {
        org.playermarket.model.WarehouseItem existing = findExistingWarehouseItem(
                warehouseItem.getOwnerUuid(),
                warehouseItem.getItemBase64(),
                warehouseItem.getOriginalUnitPrice()
        );

        if (existing != null) {
            return mergeWarehouseItem(existing.getId(), warehouseItem);
        }

        String sql = "INSERT INTO warehouse_items (owner_uuid, owner_name, item_base64, amount, original_price, original_unit_price, delisting_time, original_market_item_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, warehouseItem.getOwnerUuid().toString());
            pstmt.setString(2, warehouseItem.getOwnerName());
            pstmt.setString(3, warehouseItem.getItemBase64());
            pstmt.setInt(4, warehouseItem.getAmount());
            pstmt.setDouble(5, warehouseItem.getOriginalPrice());
            pstmt.setDouble(6, warehouseItem.getOriginalUnitPrice());
            pstmt.setTimestamp(7, warehouseItem.getDelistingTime());
            pstmt.setInt(8, warehouseItem.getOriginalMarketItemId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        warehouseItem.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "添加物品到仓库失败", e);
        }
        return false;
    }

    private org.playermarket.model.WarehouseItem findExistingWarehouseItem(UUID ownerUuid, String itemBase64, double originalUnitPrice) {
        long unitPriceInt = Math.round(originalUnitPrice * 100);
        String sql = "SELECT * FROM warehouse_items WHERE owner_uuid = ? AND item_base64 = ? ORDER BY delisting_time DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUuid.toString());
            pstmt.setString(2, itemBase64);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    org.playermarket.model.WarehouseItem item = resultSetToWarehouseItem(rs);
                    long existingUnitPriceInt = Math.round(item.getOriginalUnitPrice() * 100);
                    if (existingUnitPriceInt == unitPriceInt) {
                        return item;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "查找已存在仓库物品失败", e);
        }

        return null;
    }

    private boolean mergeWarehouseItem(int existingItemId, org.playermarket.model.WarehouseItem newItem) {
        org.playermarket.model.WarehouseItem existing = getWarehouseItemById(existingItemId);
        if (existing == null) {
            return false;
        }

        int newAmount = existing.getAmount() + newItem.getAmount();
        double newPrice = existing.getOriginalUnitPrice() * newAmount;

        String sql = "UPDATE warehouse_items SET amount = ?, original_price = ?, delisting_time = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newAmount);
            pstmt.setDouble(2, newPrice);
            pstmt.setInt(3, existingItemId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                org.playermarket.model.WarehouseItem merged = getWarehouseItemById(existingItemId);
                if (merged != null) {
                    newItem.setId(merged.getId());
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "合并仓库物品失败", e);
        }

        return false;
    }
    
    // 获取仓库物品（分页）
    public List<org.playermarket.model.WarehouseItem> getWarehouseItems(UUID ownerUuid, int page, int pageSize) {
        List<org.playermarket.model.WarehouseItem> items = new ArrayList<>();
        String sql = "SELECT * FROM warehouse_items WHERE owner_uuid = ? ORDER BY delisting_time DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUuid.toString());
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(resultSetToWarehouseItem(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取仓库物品失败", e);
        }
        
        return items;
    }
    
    // 获取仓库物品数量
    public int getWarehouseItemsCount(UUID ownerUuid) {
        String sql = "SELECT COUNT(*) FROM warehouse_items WHERE owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取仓库物品数量失败", e);
        }
        return 0;
    }
    
    // 从仓库取出物品
    public boolean withdrawWarehouseItem(int itemId, UUID ownerUuid) {
        String sql = "DELETE FROM warehouse_items WHERE id = ? AND owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setString(2, ownerUuid.toString());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "从仓库取出物品失败", e);
        }
        return false;
    }
    
    // 从仓库部分取出物品
    public boolean partialWithdrawWarehouseItem(int itemId, UUID ownerUuid, int amount) {
        if (amount <= 0) {
            return false;
        }

        String sql = "UPDATE warehouse_items SET amount = amount - ? WHERE id = ? AND owner_uuid = ? AND amount >= ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setInt(2, itemId);
            pstmt.setString(3, ownerUuid.toString());
            pstmt.setInt(4, amount);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "从仓库部分取出物品失败", e);
            return false;
        }

        return withdrawWarehouseItem(itemId, ownerUuid);
    }
    
    // 根据ID获取仓库物品
    public org.playermarket.model.WarehouseItem getWarehouseItemById(int itemId) {
        String sql = "SELECT * FROM warehouse_items WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToWarehouseItem(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "根据ID获取仓库物品失败", e);
        }
        return null;
    }
    
    // 将ResultSet转换为WarehouseItem对象
    private org.playermarket.model.WarehouseItem resultSetToWarehouseItem(ResultSet rs) throws SQLException {
        org.playermarket.model.WarehouseItem item = new org.playermarket.model.WarehouseItem();
        item.setId(rs.getInt("id"));
        item.setOwnerUuid(UUID.fromString(rs.getString("owner_uuid")));
        item.setOwnerName(rs.getString("owner_name"));
        item.setItemBase64(rs.getString("item_base64"));
        item.setAmount(rs.getInt("amount"));
        item.setOriginalPrice(rs.getDouble("original_price"));
        item.setOriginalUnitPrice(rs.getDouble("original_unit_price"));
        item.setDelistingTime(rs.getTimestamp("delisting_time"));
        item.setOriginalMarketItemId(rs.getInt("original_market_item_id"));
        return item;
    }
    
    // 将ResultSet转换为MarketItem对象
    private MarketItem resultSetToMarketItem(ResultSet rs) throws SQLException {
        MarketItem item = new MarketItem();
        item.setId(rs.getInt("id"));
        item.setSellerUuid(UUID.fromString(rs.getString("seller_uuid")));
        item.setSellerName(rs.getString("seller_name"));
        item.setItemBase64(rs.getString("item_base64"));
        item.setAmount(rs.getInt("amount"));
        
        // 尝试获取单价，如果不存在则自动计算
        try {
            item.setUnitPrice(rs.getDouble("unit_price"));
        } catch (SQLException e) {
            // unit_price 列不存在，自动计算
            double price = rs.getDouble("price");
            int amount = rs.getInt("amount");
            if (amount > 0) {
                item.setUnitPrice(price / amount);
            } else {
                item.setUnitPrice(price);
            }
        }
        
        item.setPrice(rs.getDouble("price"));
        item.setListTime(rs.getTimestamp("list_time"));
        item.setSold(rs.getBoolean("sold"));
        
        String buyerUuidStr = rs.getString("buyer_uuid");
        if (buyerUuidStr != null) {
            item.setBuyerUuid(UUID.fromString(buyerUuidStr));
        }
        
        item.setBuyerName(rs.getString("buyer_name"));
        item.setSoldTime(rs.getTimestamp("sold_time"));
        
        return item;
    }
    
    // 添加通知
    public boolean addNotification(UUID playerUuid, String playerName, String notificationType, String message, String data) {
        String sql = "INSERT INTO notifications (player_uuid, player_name, notification_type, message, data, created_time) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, playerName);
            pstmt.setString(3, notificationType);
            pstmt.setString(4, message);
            pstmt.setString(5, data);
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "添加通知失败", e);
            return false;
        }
    }
    
    // 获取玩家未读通知
    public List<String> getUnreadNotifications(UUID playerUuid) {
        List<String> notifications = new ArrayList<>();
        String sql = "SELECT message, data, created_time FROM notifications WHERE player_uuid = ? AND read = FALSE ORDER BY created_time ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String message = rs.getString("message");
                    String data = rs.getString("data");
                    Timestamp createdTime = rs.getTimestamp("created_time");
                    notifications.add(message + " §7[" + formatTime(createdTime) + "]");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取未读通知失败", e);
        }
        
        return notifications;
    }
    
    // 标记通知为已读
    public boolean markNotificationsAsRead(UUID playerUuid) {
        String sql = "UPDATE notifications SET read = TRUE WHERE player_uuid = ? AND read = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "标记通知为已读失败", e);
            return false;
        }
    }
    
    // 清理旧通知（保留最近30天）
    public void cleanupOldNotifications() {
        String sql = "DELETE FROM notifications WHERE created_time < datetime('now', '-30 days') AND read = TRUE";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "清理旧通知失败", e);
        }
    }
    
    // 格式化时间显示
    private String formatTime(java.sql.Timestamp timestamp) {
        long timestampMillis = timestamp.getTime();
        long currentMillis = System.currentTimeMillis();
        
        // 计算时间差（毫秒）
        long diff = currentMillis - timestampMillis;
        
        // 转换为分钟
        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " 天前";
        } else if (hours > 0) {
            return hours + " 小时前";
        } else if (minutes > 0) {
            return minutes + " 分钟前";
        } else {
            return "刚刚";
        }
    }
    
    // 关闭数据库连接
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("数据库连接已关闭");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "关闭数据库连接失败", e);
        }
    }
    
    // ==================== 收购订单相关方法 ====================
    
    // 添加收购订单（支持合并）
    public boolean addBuyOrder(BuyOrder buyOrder) {
        // 先检查是否存在相同买家、相同物品、相同单价的未完成收购订单
        BuyOrder existingOrder = findExistingBuyOrder(buyOrder.getBuyerUuid(), buyOrder.getItemBase64(), buyOrder.getUnitPrice());
        
        if (existingOrder != null && !existingOrder.isFulfilled() && existingOrder.getRemainingAmount() > 0) {
            // 合并收购订单：更新数量和总价
            return mergeBuyOrder(existingOrder.getId(), buyOrder);
        } else {
            // 插入新收购订单
            return insertBuyOrder(buyOrder);
        }
    }
    
    // 查找已存在的收购订单（用于合并）
    private BuyOrder findExistingBuyOrder(UUID buyerUuid, String itemBase64, double unitPrice) {
        // 使用整数比较避免浮点数精度问题（乘以100后比较）
        long unitPriceInt = Math.round(unitPrice * 100);
        
        String sql = "SELECT * FROM buy_orders WHERE buyer_uuid = ? AND item_base64 = ? AND fulfilled = FALSE AND remaining_amount > 0 ORDER BY create_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, buyerUuid.toString());
            pstmt.setString(2, itemBase64);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BuyOrder order = resultSetToBuyOrder(rs);
                    // 使用整数比较避免浮点数精度问题
                    long existingUnitPriceInt = Math.round(order.getUnitPrice() * 100);
                    if (existingUnitPriceInt == unitPriceInt) {
                        return order;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "查找已存在收购订单失败", e);
        }
        
        return null;
    }
    
    // 合并收购订单（累加数量）
    private boolean mergeBuyOrder(int existingOrderId, BuyOrder newOrder) {
        // 先获取现有收购订单信息
        BuyOrder existingOrder = getBuyOrderById(existingOrderId);
        if (existingOrder == null || existingOrder.isFulfilled()) {
            return false;
        }
        
        // 计算新数量
        int newAmount = existingOrder.getAmount() + newOrder.getAmount();
        int newRemainingAmount = existingOrder.getRemainingAmount() + newOrder.getRemainingAmount();
        
        // 计算新总价（使用单价 * 新数量）
        double newTotalPrice = existingOrder.getUnitPrice() * newAmount;
        double newRemainingTotalPrice = existingOrder.getUnitPrice() * newRemainingAmount;
        
        String sql = "UPDATE buy_orders SET amount = ?, remaining_amount = ?, total_price = ?, remaining_total_price = ?, create_time = CURRENT_TIMESTAMP WHERE id = ? AND fulfilled = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newAmount);
            pstmt.setInt(2, newRemainingAmount);
            pstmt.setDouble(3, newTotalPrice);
            pstmt.setDouble(4, newRemainingTotalPrice);
            pstmt.setInt(5, existingOrderId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // 获取更新后的订单信息
                BuyOrder mergedOrder = getBuyOrderById(existingOrderId);
                if (mergedOrder != null) {
                    newOrder.setId(mergedOrder.getId());
                }
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "合并收购订单失败", e);
        }
        return false;
    }
    
    // 插入新收购订单
    private boolean insertBuyOrder(BuyOrder buyOrder) {
        String sql = "INSERT INTO buy_orders (buyer_uuid, buyer_name, item_base64, amount, remaining_amount, unit_price, total_price, remaining_total_price, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, buyOrder.getBuyerUuid().toString());
            pstmt.setString(2, buyOrder.getBuyerName());
            pstmt.setString(3, buyOrder.getItemBase64());
            pstmt.setInt(4, buyOrder.getAmount());
            pstmt.setInt(5, buyOrder.getRemainingAmount());
            pstmt.setDouble(6, buyOrder.getUnitPrice());
            pstmt.setDouble(7, buyOrder.getTotalPrice());
            pstmt.setDouble(8, buyOrder.getRemainingTotalPrice());
            pstmt.setTimestamp(9, buyOrder.getCreateTime());
            pstmt.executeUpdate();
            
            // 获取生成的ID
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    buyOrder.setId(rs.getInt(1));
                }
            }
            
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "插入收购订单失败", e);
            return false;
        }
    }
    
    // 获取所有未完成的收购订单
    public List<BuyOrder> getAllUnfulfilledBuyOrders() {
        List<BuyOrder> buyOrders = new ArrayList<>();
        String sql = "SELECT * FROM buy_orders WHERE fulfilled = FALSE AND remaining_amount > 0 ORDER BY create_time DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                buyOrders.add(resultSetToBuyOrder(rs));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取收购订单失败", e);
        }
        
        return buyOrders;
    }
    
    // 获取玩家的收购订单
    public List<BuyOrder> getPlayerBuyOrders(UUID playerUuid) {
        List<BuyOrder> buyOrders = new ArrayList<>();
        String sql = "SELECT * FROM buy_orders WHERE buyer_uuid = ? ORDER BY create_time DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerUuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    buyOrders.add(resultSetToBuyOrder(rs));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "获取玩家收购订单失败", e);
        }
        
        return buyOrders;
    }
    
    // 卖家向收购订单出售（支持部分成交）
    public boolean sellToBuyOrder(int orderId, UUID sellerUuid, String sellerName, int sellAmount) {
        if (sellAmount <= 0) {
            return false;
        }

        BuyOrder order = getBuyOrderById(orderId);
        if (order == null || order.isFulfilled()) {
            return false;
        }

        if (order.getRemainingAmount() <= 0 || sellAmount > order.getRemainingAmount()) {
            return false;
        }

        int newRemainingAmount = order.getRemainingAmount() - sellAmount;
        double newRemainingTotalPrice = order.getUnitPrice() * newRemainingAmount;

        String sql;
        if (newRemainingAmount <= 0) {
            sql = "UPDATE buy_orders SET remaining_amount = 0, remaining_total_price = 0, fulfilled = TRUE, fulfill_time = ? WHERE id = ? AND fulfilled = FALSE AND remaining_amount >= ?";
        } else {
            sql = "UPDATE buy_orders SET remaining_amount = ?, remaining_total_price = ? WHERE id = ? AND fulfilled = FALSE AND remaining_amount >= ?";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            if (newRemainingAmount <= 0) {
                pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                pstmt.setInt(2, orderId);
                pstmt.setInt(3, sellAmount);
            } else {
                pstmt.setInt(1, newRemainingAmount);
                pstmt.setDouble(2, newRemainingTotalPrice);
                pstmt.setInt(3, orderId);
                pstmt.setInt(4, sellAmount);
            }

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "出售给收购订单失败", e);
            return false;
        }
    }

    public BuyOrder getBuyOrderById(int orderId) {
        String sql = "SELECT * FROM buy_orders WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return resultSetToBuyOrder(rs);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "根据ID获取收购订单失败", e);
        }

        return null;
    }
    
    // 取消收购订单
    public boolean cancelBuyOrder(int orderId, UUID buyerUuid) {
        String sql = "DELETE FROM buy_orders WHERE id = ? AND buyer_uuid = ? AND fulfilled = FALSE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setString(2, buyerUuid.toString());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "取消收购订单失败", e);
            return false;
        }
    }
    
    // 将收购订单标记为已完成
    public boolean markBuyOrderAsFulfilled(int orderId, UUID buyerUuid) {
        String sql = "UPDATE buy_orders SET fulfilled = TRUE, fulfill_time = ? WHERE id = ? AND buyer_uuid = ? AND fulfilled = FALSE AND remaining_amount = 0";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(2, orderId);
            pstmt.setString(3, buyerUuid.toString());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "标记收购订单为已完成失败", e);
            return false;
        }
    }
    
    // 提取部分完成订单中已收购的物品：将amount/total_price同步为remaining_amount/remaining_total_price，防止重复提货
    public boolean claimBuyOrderAcquiredItems(int orderId, UUID buyerUuid, int acquiredAmount) {
        if (acquiredAmount <= 0) {
            return false;
        }

        String sql = "UPDATE buy_orders SET amount = remaining_amount, total_price = remaining_total_price " +
                "WHERE id = ? AND buyer_uuid = ? AND fulfilled = FALSE AND (amount - remaining_amount) = ? AND remaining_amount > 0";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setString(2, buyerUuid.toString());
            pstmt.setInt(3, acquiredAmount);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "提取已收购物品失败", e);
            return false;
        }
    }
    
    // 删除已完成的收购订单
    public boolean deleteCompletedBuyOrder(int orderId, UUID buyerUuid) {
        String sql = "DELETE FROM buy_orders WHERE id = ? AND buyer_uuid = ? AND fulfilled = TRUE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            pstmt.setString(2, buyerUuid.toString());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "删除已完成收购订单失败", e);
            return false;
        }
    }
    
    // 更新收购订单数量
    public boolean updateBuyOrderAmount(int orderId, UUID buyerUuid, int newAmount) {
        if (newAmount <= 0) {
            return false;
        }
        
        try {
            // 先获取当前订单信息
            String selectSql = "SELECT * FROM buy_orders WHERE id = ? AND buyer_uuid = ? AND fulfilled = FALSE";
            int currentAmount = 0;
            int currentRemainingAmount = 0;
            double unitPrice = 0;
            
            try (PreparedStatement pstmt = connection.prepareStatement(selectSql)) {
                pstmt.setInt(1, orderId);
                pstmt.setString(2, buyerUuid.toString());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        currentAmount = rs.getInt("amount");
                        currentRemainingAmount = rs.getInt("remaining_amount");
                        unitPrice = rs.getDouble("unit_price");
                    }
                }
            }
            
            if (currentRemainingAmount == 0) {
                return false;
            }
            
            // 计算新的总量和剩余量
            // 剩余量不能为负数，且新总量不能小于已成交的数量
            int soldAmount = currentAmount - currentRemainingAmount;
            if (newAmount < soldAmount) {
                return false;
            }
            
            int newRemainingAmount = newAmount - soldAmount;
            double newTotalPrice = unitPrice * newAmount;
            double newRemainingTotalPrice = unitPrice * newRemainingAmount;
            
            // 更新数量和价格
            String updateSql = "UPDATE buy_orders SET amount = ?, remaining_amount = ?, total_price = ?, remaining_total_price = ? WHERE id = ? AND buyer_uuid = ? AND fulfilled = FALSE";
            
            try (PreparedStatement pstmt = connection.prepareStatement(updateSql)) {
                pstmt.setInt(1, newAmount);
                pstmt.setInt(2, newRemainingAmount);
                pstmt.setDouble(3, newTotalPrice);
                pstmt.setDouble(4, newRemainingTotalPrice);
                pstmt.setInt(5, orderId);
                pstmt.setString(6, buyerUuid.toString());
                
                int affectedRows = pstmt.executeUpdate();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "更新收购订单数量失败", e);
        }
        return false;
    }
    
    // ResultSet转BuyOrder
    private BuyOrder resultSetToBuyOrder(ResultSet rs) throws SQLException {
        BuyOrder buyOrder = new BuyOrder();
        buyOrder.setId(rs.getInt("id"));
        buyOrder.setBuyerUuid(UUID.fromString(rs.getString("buyer_uuid")));
        buyOrder.setBuyerName(rs.getString("buyer_name"));
        buyOrder.setItemBase64(rs.getString("item_base64"));
        buyOrder.setAmount(rs.getInt("amount"));
        buyOrder.setRemainingAmount(rs.getInt("remaining_amount"));
        buyOrder.setUnitPrice(rs.getDouble("unit_price"));
        buyOrder.setTotalPrice(rs.getDouble("total_price"));
        buyOrder.setRemainingTotalPrice(rs.getDouble("remaining_total_price"));
        buyOrder.setCreateTime(rs.getTimestamp("create_time"));
        buyOrder.setFulfilled(rs.getBoolean("fulfilled"));

        buyOrder.setSellerUuid(null);
        buyOrder.setSellerName(null);
        buyOrder.setFulfillTime(rs.getTimestamp("fulfill_time"));

        return buyOrder;
    }
}