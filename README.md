## PlayerMarket

### 🎯 简介（Introduction）

**中文**：玩家间的中央交易市场系统，支持商品买卖、求购订单、仓库管理等功能。  
**English**：A central trading market system for players, supporting item selling, buy orders, warehouse management, and more.


### ✨ 核心功能（Core Features）

**中文**：
- 购买市场：浏览和购买其他玩家上架的商品
- 求购市场：发布求购订单，等待其他玩家出售
- 我的上架：管理自己上架的商品（调整数量、下架）
- 我的收购：管理自己发布的求购订单（修改数量、取消）
- 我的仓库：存放购买或收购的物品，支持一键取出

**English**：
- Buy Market: Browse and purchase items listed by other players
- Buy Order Market: Create buy orders and wait for other players to sell
- My Listings: Manage your listed items (adjust quantity, delist)
- My Buy Orders: Manage your buy orders (modify quantity, cancel)
- Warehouse: Store purchased or acquired items, with one-click withdrawal


### 🌐 双语支持与语言系统（Bilingual Support & Language System）

**中文**：
- 完整双语界面：支持中文（简体）和英文（美国）
- 智能语言检测：自动检测玩家客户端语言
- 服务器默认语言：管理员可设置服务器默认语言
- 个性化语言设置：玩家可随时切换语言（`/pm lang <zh_CN|en_US|auto>`）
- Auto模式：使用服务器默认语言，跟随管理员设置自动更新

**English**：
- Full bilingual interface: Supports Chinese (Simplified) and English (US)
- Smart language detection: Automatically detects player's client language
- Server default language: Admins can set server default language
- Personalized language settings: Players can switch language anytime (`/pm lang <zh_CN|en_US|auto>`)
- Auto mode: Uses server default language, automatically updates with admin changes


### 🔔 通知系统（Notification System）

**中文**：
- 实时交易通知：购买/出售/求购完成时即时通知
- 离线通知保存：玩家不在线时通知会保存，下次登录时显示
- 重复通知过滤：在线时已读通知不会重复显示
- 通知分类：支持购买、出售、求购等多种通知类型

**English**：
- Real-time transaction notifications: Instant notifications for purchases/sales/buy orders
- Offline notification storage: Notifications saved when player is offline, shown on next login
- Duplicate notification filtering: Read notifications not shown again
- Notification categories: Supports purchase, sale, buy order, and other notification types


### 📦 安装方法（Installation）

**中文**：
1. 前置依赖：Paper 1.21+、Vault 插件、兼容的经济插件
2. 安装步骤：下载 JAR 文件到 `plugins/` 目录，重启服务器

**English**：
1. Dependencies: Paper 1.21+, Vault plugin, compatible economy plugin
2. Installation: Download the JAR file to `plugins/` directory, restart server


### 🚀 使用指南（Usage）

**中文**：
- 打开市场：`/playermarket` 或 `/pm`
- 快速上架：`/manuela <数量> <单价>`
- 快速求购：`/pur <数量> <单价>`
- 查看余额：`/pm balance`
- 设置语言：`/pm lang <zh_CN|en_US|auto>`
- 管理员命令：`/pm reload` (重载配置), `/pm defaultlang <zh_CN|en_US>` (设置默认语言)

**English**：
- Open market: `/playermarket` or `/pm`
- Quick listing: `/manuela <quantity> <price>`
- Quick buy order: `/pur <quantity> <price>`
- Check balance: `/pm balance`
- Set language: `/pm lang <zh_CN|en_US|auto>`
- Admin commands: `/pm reload` (reload config), `/pm defaultlang <zh_CN|en_US>` (set default language)


### �️ 数据库支持（Database Support）

**中文**：
- 内置SQLite数据库：无需额外配置，自动管理
- 数据持久化：市场商品、求购订单、玩家仓库数据安全存储
- 自动备份：数据库文件自动保存，服务器重启数据不丢失
- 轻量级设计：高效的数据结构和查询优化

**English**：
- Built-in SQLite database: No additional configuration needed, automatically managed
- Data persistence: Market items, buy orders, warehouse data securely stored
- Automatic backup: Database file automatically saved, data preserved on server restart
- Lightweight design: Efficient data structure and query optimization


### �🛠️ 配置与权限（Configuration & Permissions）

**中文**：
- 配置文件：`plugins/PlayerMarket/config.yml`
- 权限节点：`playermarket.use`（基本权限）、`playermarket.admin`（管理权限）

**English**：
- Config file: `plugins/PlayerMarket/config.yml`
- Permission nodes: `playermarket.use` (basic permission), `playermarket.admin` (admin permission)


### ⚙️ 配置示例（Configuration Example）

```yaml
# 市场界面设置
market:
  title: "§6玩家市场 §7| §a余额: {balance}"
  size: 54
  auto-refresh: true
  refresh-interval: 30

# 经济系统设置
economy:
  enabled: true
  check-on-open: true
  display-balance: true

# 交易设置
transaction:
  enabled: true
  fee-percentage: 1.5
  min-price: 0.01
  max-price: 1000000.0

# 语言设置
language:
  default: "zh_CN"
  auto-detect: true
```


### 📋 命令详细说明（Command Details）

| 命令 | 描述 | 权限 | 参数 |
|------|------|------|------|
| `/playermarket` | 打开市场主界面 | `playermarket.use` | 无 |
| `/playermarket balance` | 查看个人余额 | `playermarket.use` | 无 |
| `/playermarket lang` | 设置个人语言 | `playermarket.use` | `<zh_CN\|en_US\|auto>` |
| `/playermarket defaultlang` | 设置服务器默认语言 | `playermarket.admin` | `<zh_CN\|en_US>` |
| `/playermarket reload` | 重载配置文件 | `playermarket.admin` | 无 |
| `/playermarket debug` | 显示调试信息 | `playermarket.admin` | 无 |
| `/manuela` | 快速上架手中物品 | `playermarket.use` | `<数量> <单价>` |
| `/pur` | 快速发布求购订单 | `playermarket.use` | `<数量> <单价>` |


### 📊 更新日志（Changelog）

#### v1.0
- ✨ 初始版本发布：完整的市场交易系统
- 🌐 双语支持：中英文界面，智能语言检测
- 🔔 通知系统：实时交易通知，离线通知保存
- 🗃️ 数据库：SQLite存储，数据持久化
- 💰 经济集成：Vault支持，兼容多种经济插件
- 📱 GUI界面：直观的图形操作界面
- 🔒 权限系统：基于权限节点的访问控制


### ❓ 常见问题（FAQ）

**Q: 购买商品后物品在哪里？**  
**A:** 购买的物品会自动存入 **我的仓库**，需手动取出。

**Q: 求购订单完成后物品在哪里？**  
**A:** 收购的物品会自动存入 **我的仓库**，需手动取出。

**Q: 经济系统不可用怎么办？**  
**A:** 请确保已安装 Vault 插件和兼容的经济插件，并正确配置。

**Q: 权限不足怎么办？**  
**A:** 联系服务器管理员为您分配 `playermarket.use` 权限。

**Q: 如何切换界面语言？**  
**A:** 使用命令 `/pm lang zh_CN`（中文）或 `/pm lang en_US`（英文）或 `/pm lang auto`（自动）。