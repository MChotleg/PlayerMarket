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


### 🗃️ 数据库支持（Database Support）

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


### 🛠️ 配置与权限（Configuration & Permissions）

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
   provider: "default"
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

### 💰 经济系统配置详解（Economy Configuration Details）

**中文**：
PlayerMarket 插件通过 **Vault API** 与各种经济插件兼容。`economy.provider` 接口允许服务器管理员指定使用哪个经济插件来处理货币交易。

#### 🔌 Provider 接口配置模式

1. **自动选择模式**（默认）
   - 配置：`provider: "default"`
   - 行为：自动选择第一个可用的 Vault 兼容经济插件
   - 示例：服务器同时安装了 EssentialsX 和 PlayerPoints 时，会优先选择 EssentialsX

2. **指定插件模式**
   - 配置：`provider: "插件名称"`（如 `"Essentials"`）
   - 行为：强制使用指定名称的经济插件
   - 示例：`provider: "PlayerPoints"` 强制使用 PlayerPoints 插件

#### 🔧 配置参数详解

- **`enabled`**：启用/禁用整个经济系统（默认：`true`）
- **`provider`**：经济插件选择接口（默认：`"default"`）
- **`check-on-open`**：打开市场时检查经济系统状态（默认：`true`）
- **`display-balance`**：在界面中显示玩家余额（默认：`true`）

#### 💱 货币格式配置（预留接口）
以下配置项当前版本暂未实现，作为未来扩展的预留接口：
- **`format.symbol`**：自定义货币符号（如 "$", "¥", "金币"）
- **`format.unit-name`**：自定义货币单位名称（如 "金币", "钻石"）
- **`format.decimal-places`**：小数位数（-1=插件默认，0=无小数，1-4=指定位数）
- **`format.thousands-separator`**：是否显示千位分隔符
- **`format.always-show-decimals`**：是否始终显示小数部分

#### ✅ 支持的经济插件
- Essentials/EssentialsX
- CMI
- PlayerPoints
- TokenEnchant
- GemsEconomy
- 任何其他 Vault 兼容的经济插件

**English**：
PlayerMarket plugin is compatible with various economy plugins through **Vault API**. The `economy.provider` interface allows server administrators to specify which economy plugin to use for currency transactions.

#### 🔌 Provider Interface Modes

1. **Auto-select Mode** (Default)
   - Configuration: `provider: "default"`
   - Behavior: Automatically selects the first available Vault-compatible economy plugin
   - Example: When both EssentialsX and PlayerPoints are installed, EssentialsX will be selected first

2. **Specified Plugin Mode**
   - Configuration: `provider: "plugin-name"` (e.g., `"Essentials"`)
   - Behavior: Forces the use of the specified economy plugin
   - Example: `provider: "PlayerPoints"` forces the use of PlayerPoints plugin

#### 🔧 Configuration Parameters Details

- **`enabled`**：Enable/disable the entire economy system (default: `true`)
- **`provider`**：Economy plugin selection interface (default: `"default"`)
- **`check-on-open`**：Check economy system status when opening market (default: `true`)
- **`display-balance`**：Display player balance in interface (default: `true`)

#### 💱 Currency Format Configuration (Reserved Interface)
The following configuration items are currently not implemented and reserved for future expansion:
- **`format.symbol`**：Custom currency symbol (e.g., "$", "¥", "Coins")
- **`format.unit-name`**：Custom currency unit name (e.g., "Coins", "Diamonds")
- **`format.decimal-places`**：Decimal places (-1=plugin default, 0=no decimals, 1-4=specific number)
- **`format.thousands-separator`**：Whether to display thousands separator
- **`format.always-show-decimals`**：Whether to always display decimal part

#### ✅ Supported Economy Plugins
- Essentials/EssentialsX
- CMI
- PlayerPoints
- TokenEnchant
- GemsEconomy
- Any other Vault-compatible economy plugin

### 📋 命令详细说明（Command Details）

| 命令 | 描述 (中文) | English Description | 权限 | 参数 |
|------|--------------|---------------------|------|------|
| `/playermarket` | 打开市场主界面 | Open market main interface | `playermarket.use` | 无 (none) |
| `/playermarket balance` | 查看个人余额 | Check personal balance | `playermarket.use` | 无 (none) |
| `/playermarket lang` | 设置个人语言 | Set personal language | `playermarket.use` | `<zh_CN\|en_US\|auto>` |
| `/playermarket defaultlang` | 设置服务器默认语言 | Set server default language | `playermarket.admin` | `<zh_CN\|en_US>` |
| `/playermarket reload` | 重载配置文件 | Reload configuration file | `playermarket.admin` | 无 (none) |
| `/playermarket debug` | 显示调试信息 | Display debug information | `playermarket.admin` | 无 (none) |
| `/manuela` | 快速上架手中物品 | Quickly list held item | `playermarket.use` | `<数量> <单价>` (quantity price) |
| `/pur` | 快速发布求购订单 | Quickly create buy order | `playermarket.use` | `<数量> <单价>` (quantity price) |


### 📊 更新日志（Changelog）

#### v1.1
**中文**：
- 📝 **完善README文档**：添加详细的经济系统配置说明，重点解释`provider`接口工作原理
- 🔧 **增强经济配置**：在`config.yml`中添加`economy.provider`接口和`economy.format`预留配置
- 📖 **文档结构优化**：添加"💰 经济系统配置详解"章节，提供中英文双语说明
- 🏗️ **架构重构**：将监听器拆分为模块化文件（BaseMarketListener + 专用监听器）提升维护性
- 🌍 **国际化改进**：增强物品名称本地化，支持玩家语言智能切换
- 🐛 **PurpurMC兼容性**：修复颜色代码引发的GUI异常问题
- 📦 **版本兼容性**：支持Paper API 1.20.6+（兼容1.21.11 Purpur服务器）
- 🛠️ **开发工具**：创建标准化插件模板技能（minecraft-plugin-template），便于快速新项目开发
- ⚙️ **配置简化**：简化`config.yml`结构，移除冗余选项

**English**：
- 📝 **Enhanced README documentation**: Added detailed economy system configuration explanation, focusing on `provider` interface mechanism
- 🔧 **Enhanced economy configuration**: Added `economy.provider` interface and `economy.format` reserved configuration to `config.yml`
- 📖 **Document structure optimization**: Added "💰 Economy Configuration Details" section with bilingual explanations
- 🏗️ **Architecture refactoring**: Split listeners into modular files (BaseMarketListener + specialized listeners) for better maintainability
- 🌍 **Internationalization improvement**: Enhanced item name localization with player language smart switching
- 🐛 **PurpurMC compatibility**: Fixed GUI exceptions caused by color codes
- 📦 **Version compatibility**: Supports Paper API 1.20.6+ (compatible with 1.21.11 Purpur server)
- 🛠️ **Development tools**: Created standardized plugin template skill (minecraft-plugin-template) for quick new project setup
- ⚙️ **Configuration simplification**: Simplified `config.yml` structure, removed redundant options

#### v1.0
**中文**：
- ✨ 初始版本发布：完整的市场交易系统
- 🌐 双语支持：中英文界面，智能语言检测
- 🔔 通知系统：实时交易通知，离线通知保存
- 🗃️ 数据库：SQLite存储，数据持久化
- 💰 经济集成：Vault支持，兼容多种经济插件
- 📱 GUI界面：直观的图形操作界面
- 🔒 权限系统：基于权限节点的访问控制

**English**：
- ✨ Initial version release: Complete market trading system
- 🌐 Bilingual support: Chinese-English interface with smart language detection
- 🔔 Notification system: Real-time transaction notifications with offline storage
- 🗃️ Database: SQLite storage with data persistence
- 💰 Economy integration: Vault support, compatible with multiple economy plugins
- 📱 GUI interface: Intuitive graphical operation interface
- 🔒 Permission system: Permission node-based access control


### ❓ 常见问题（FAQ）

**Q: 购买商品后物品在哪里？** / **Q: Where do items go after purchase?**  
**A:** 购买的物品会自动存入 **我的仓库**，需手动取出。 / **A:** Purchased items are automatically stored in **My Warehouse**, need to be manually withdrawn.

**Q: 求购订单完成后物品在哪里？** / **Q: Where do items go after buy order completion?**  
**A:** 收购的物品会自动存入 **我的仓库**，需手动取出。 / **A:** Acquired items are automatically stored in **My Warehouse**, need to be manually withdrawn.

**Q: 经济系统不可用怎么办？** / **Q: What if economy system is unavailable?**  
**A:** 请确保已安装 Vault 插件和兼容的经济插件，并正确配置。 / **A:** Make sure Vault plugin and compatible economy plugin are installed and properly configured.

**Q: 权限不足怎么办？** / **Q: What if I don't have permission?**  
**A:** 联系服务器管理员为您分配 `playermarket.use` 权限。 / **A:** Contact server administrator to assign you `playermarket.use` permission.

**Q: 如何切换界面语言？** / **Q: How to switch interface language?**  
**A:** 使用命令 `/pm lang zh_CN`（中文）或 `/pm lang en_US`（英文）或 `/pm lang auto`（自动）。 / **A:** Use command `/pm lang zh_CN` (Chinese) or `/pm lang en_US` (English) or `/pm lang auto` (auto).
