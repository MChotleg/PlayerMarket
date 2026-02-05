# 🛒 PlayerMarket - Modern Player Trading System

> **A comprehensive player-to-player market solution for your server**

![Main Menu](https://www.spigotmc.org/attachments/upload_2026-2-5_8-59-31-png.943378/?temp_hash=847849a8261b5edcc87253fa668aff82)
*(Clean and intuitive 54-slot Main Menu GUI)*

---

## ✨ Introduction
**PlayerMarket** is a comprehensive trading system designed for server economies. It not only allows players to list items for sale but also introduces an innovative **"Buy Order Market"**, enabling buyers to post requests and wait for sellers to fulfill them. With its convenient GUI, warehouse system, and bilingual support, it is the perfect addition to your server's economy.

## 🔥 Key Features

### 🛒 Dual Market System
*   **Buying Market**: Browse items listed by players across the server and find what you need.
*   **Buy Order Market**: Need materials urgently? Post a buy order and let players with stock come to you!

![Dual Market](https://www.spigotmc.org/attachments/upload_2026-2-5_8-59-47-png.943379/?temp_hash=847849a8261b5edcc87253fa668aff82)

### 🏪 Personal Player Shop
*   **Personal Showcase**: Each player gets their own shop page to display all their active listings and buy orders.
*   **Shop Management**: Owners can check transaction history (sales/purchases) and manage listings with ease.
*   **Quick Access**: Click any item in the global market to jump directly to the seller's personal shop.

![Player Shop](https://www.spigotmc.org/attachments/upload_2026-2-5_9-0-2-png.943380/?temp_hash=847849a8261b5edcc87253fa668aff82)

### 🔢 Optimized Bulk Operations
*   **Visual Quantity Control**: No more tedious commands! Use GUI buttons (+1, +10, +64, All) to quickly adjust quantities when buying or listing.
*   **Large Transaction Friendly**: Whether buying stacks of building blocks or selling a full inventory of ores, complete large trades with just a few clicks.

![Bulk Operations](https://www.spigotmc.org/attachments/upload_2026-2-5_9-0-25-png.943381/?temp_hash=847849a8261b5edcc87253fa668aff82)

### 📦 Smart Warehouse Management
*   **Auto-Storage**: All purchased items and fulfilled buy orders are safely stored in your personal warehouse.
*   **One-Click Withdrawal**: Never worry about losing items due to a full inventory. Manage your stock anytime, anywhere.

### 🌍 Ultimate Localization
*   **Bilingual Support**: Native support for English (en_US) and Simplified Chinese (zh_CN).
*   **Auto Detection**: Intelligently detects the player's client language for a seamless experience.

### 🛡️ Security & Management
*   **Transaction Audit**: The `/pm audit` command helps admins quickly spot suspicious high-value or frequent transactions.
*   **Featured Shops**: Admins can set up to 14 featured slots to promote quality shops.
*   **Data Safety**: Built on SQLite local database with auto-backup to ensure zero data loss.

---

## 🚀 Commands

### Player Commands
```yaml
/pm                        - Open the main market menu
/manuela <amount> <price>  - Quickly list the item in hand
/pur <amount> <price>      - Quickly post a buy order
/pm balance                - Check your wallet balance
/pm lang <zh_CN|en_US|auto> - Switch language preference
```

### Admin Commands
```yaml
/pm audit                  - Audit suspicious transactions
/pm featured <set|remove>  - Manage featured shops
/pm reload                 - Reload configuration
```

---

## 🛠️ Installation

1.  Ensure your server has **Vault** and a compatible economy plugin (e.g., EssentialsX, CMI, PlayerPoints) installed.
2.  Place `PlayerMarket-1.2.jar` into your `plugins` folder.
3.  Restart the server to generate configuration files.

## 🔗 Links

*   [GitHub Repository](https://github.com/MChotleg/PlayerMarket)
*   [Issue Tracker](https://github.com/MChotleg/PlayerMarket/issues)
