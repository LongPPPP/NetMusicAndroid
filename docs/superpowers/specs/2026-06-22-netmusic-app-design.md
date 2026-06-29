# NetMusic 网易云音乐类 App — 项目设计

> 暂定版本，需要修改

> 课程实践项目，6 人团队（最多 4 人编码），2 周开发周期
> 客户端：Android (Kotlin + 传统 Activity + XML 布局) | 服务端：Node.js + TypeScript
> 客户端开发要求：传统 Activity 开发模式，掌握 Intent 编程技术

---

## 一、项目结构策略

**方案：Monorepo 单仓库**

```
NetMusicAndroid/
├── app/                     # Android 客户端（现有结构，不动）
├── server/                  # Node.js + TypeScript 服务端
├── docs/                    # 课程文档
├── build.gradle.kts         # Android 根构建文件（不变）
├── settings.gradle.kts      # Android 设置文件（不变）
└── .gitignore               # 更新，添加 server/node_modules
```

**原则：**
- `app/` — Android 组，不修改 Gradle
- `server/` — 后端组，独立于 Android 构建系统
- `docs/` — 文档组，放所有课程交付物
- 根目录保留 Android 原生配置

---

## 二、服务端架构

### 技术栈
- **运行时：** Node.js (≥18 LTS)
- **语言：** TypeScript
- **Web 框架：** Express.js 或 Fastify
- **数据库：** MySQL / PostgreSQL（可选 SQLite 开发）
- **ORM：** Prisma 或 TypeORM
- **鉴权：** JWT (jsonwebtoken)
- **密码加密：** bcrypt

### 分层架构

```
请求 → 路由(routes) → 中间件(middlewares) → 控制器(controller) → 服务(service) → 模型(model) → 数据库
                                     ← 统一响应格式 ←
```

| 层 | 职责 | 说明 |
|----|------|------|
| `routes/` | 路由注册 | 定义 URL 映射，参数校验，不写业务逻辑 |
| `controllers/` | 请求处理 | 接收请求、调用 service、返回响应，很薄的一层 |
| `services/` | 业务逻辑 | 核心业务规则（创建歌单、权限检查等） |
| `models/` | 数据模型 | 数据库 Schema / ORM 模型定义 |
| `middlewares/` | 中间件 | 鉴权、日志、错误处理、广告注入 |

### 服务端目录结构

```
server/
├── src/
│   ├── index.ts                    # 入口
│   ├── config/
│   │   ├── index.ts                # 环境变量配置
│   │   └── database.ts             # 数据库连接
│   ├── routes/                     # 路由
│   │   ├── auth.routes.ts
│   │   ├── user.routes.ts
│   │   ├── song.routes.ts
│   │   ├── playlist.routes.ts
│   │   ├── comment.routes.ts
│   │   ├── search.routes.ts
│   │   ├── mv.routes.ts
│   │   ├── advertisement.routes.ts
│   │   └── index.ts
│   ├── controllers/                # 控制器
│   ├── services/                   # 服务
│   ├── models/                     # 数据模型
│   ├── middlewares/                 # 中间件
│   │   ├── auth.middleware.ts
│   │   ├── error.middleware.ts
│   │   ├── logger.middleware.ts
│   │   └── ad-injection.ts
│   ├── utils/                      # 工具
│   │   ├── response.ts
│   │   ├── jwt.ts
│   │   └── password.ts
│   └── types/                      # 类型定义
│       ├── index.ts
│       └── advertisement.ts
├── ad-plugin/                      # 广告插件
│   ├── index.ts
│   └── providers/
│       └── sample.ts
├── tests/
│   ├── unit/
│   └── integration/
├── package.json
├── tsconfig.json
└── .env.example
```

---

## 三、可插拔广告系统

**设计模式：策略模式 + 依赖注入**

### 工作原理

```typescript
// ① 配置开关
config.ad = {
  enabled: process.env.AD_ENABLED === 'true',
  provider: process.env.AD_PROVIDER || 'sample',
  interval: 5,  // 每 N 条内容插一条广告
}

// ② 广告提供者接口
interface AdProvider {
  getAds(context: AdContext): Promise<Ad[]>;
}

// ③ 中间件注入（内容列表响应后插入广告）
router.use(adInjectionMiddleware);
```

| 操作 | 方式 |
|------|------|
| 关闭广告 | `.env` 中 `AD_ENABLED=false` |
| 切换广告源 | 新建 provider，改 `AD_PROVIDER` |
| 完全移除 | 删 `routes/advertisement.routes.ts` + `ad-plugin/` 目录 |

### 广告植入规则
- 仅在**内容列表**响应时插入（歌单列表、推荐流、搜索结果）
- 用户信息、登录等接口不插入广告
- 广告关闭时中间件直接跳过，零开销

---

## 四、功能模块 & 团队分工

### 功能模块

| 模块 | 子功能 |
|------|--------|
| **用户模块** | 注册/登录、个人信息、收藏 |
| **音乐模块** | 歌曲播放、歌单管理、搜索、MV、歌词 |
| **社交模块** | 评论/回复、点赞 |
| **扩展功能** | 广告系统（可插拔）、深色模式/主题切换 |

### 角色分工

| 角色 | 人 | 负责 |
|------|----|------|
| 后端 A | 1人 | 用户 + 鉴权 + API 接口规范 |
| 后端 B | 1人 | 音乐模块 API |
| Android A | 1人 | Activity 框架搭建 + 用户模块 UI + 网络层封装 + Intent 导航 + 深色模式 |
| Android B | 1人 | 播放器 + 歌单 + 搜索 + 评论等核心功能 Activity UI |
| 广告模块 | （谁有空） | 后端广告接口 + Android 广告位 |
| 文档组 | 2人 | 需求分析、数据字典、DFD、设计文档、报告 |
| 项目经理 | （兼） | 进度、接口规范、联调协调 |

### 2 周冲刺节奏

**第 1 周：基础功能并行开发**
- 后端：用户系统 + 音乐核心 API（歌曲、歌单、搜索）
- 前端：Activity 框架搭建 + 网络层 + 用户 Activity 界面 + 音乐列表/搜索界面
- 前端重点：**显式 Intent 跳转、Bundle 传参、startActivityForResult**
- 文档：需求分析、数据字典、DFD

**第 2 周：集成 + 扩展功能**
- 后端：评论/MV API + 广告系统
- 前端：播放器 Activity + 评论 Activity + MV Activity + 广告位 + 深色模式实现
- 前端重点：**隐式 Intent（分享、打开链接）、Intent Filter、主题动态切换**
- 文档：概要设计、详细设计
- 周五前：联调 + 最终测试

---

## 五、API 设计规范（草案）

### 基础路径
```
http://localhost:3000/api/v1
```

### 统一响应格式
```typescript
// 成功
{
  "code": 200,
  "message": "success",
  "data": { ... }
}

// 失败
{
  "code": 400,
  "message": "error description",
  "data": null
}
```

### 主要接口列表
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 注册 |
| POST | `/auth/login` | 登录 |
| GET | `/users/:id` | 用户信息 |
| GET | `/songs` | 歌曲列表 |
| GET | `/songs/:id` | 歌曲详情（含歌词）|
| GET | `/playlists` | 歌单列表 |
| GET | `/playlists/:id` | 歌单详情（含歌曲列表）|
| POST | `/playlists` | 创建歌单 |
| GET | `/search?q=&type=` | 搜索 |
| GET | `/mvs` | MV 列表 |
| GET | `/mvs/:id` | MV 详情 |
| POST | `/comments` | 发表评论 |
| GET | `/comments/:targetId` | 获取评论 |
| GET | `/ads` | 获取广告（供客户端）|

---

## 六、Android 客户端架构

### 6.1 开发模式

采用**传统 Activity 开发模式**（非 Jetpack Compose）：

- **UI 构建：** XML 布局文件（`res/layout/`）
- **Activity 基类：** 继承 `AppCompatActivity`
- **导航方式：** **Intent 编程**（显式 Intent + 隐式 Intent）
- **主题系统：** `styles.xml` + `AppCompat.DayNight` 兼容主题

### 6.2 Activity 架构设计

```
Application
│
├── SplashActivity              # 启动页/广告页
├── MainActivity                # 主界面（底部导航栏承载容器）
│   ├── 使用 Fragment 或 ActivityGroup 切换页面
│   └── 通过 Intent 跳转到其他 Activity 后返回
│
├── LoginActivity               # 登录
├── RegisterActivity            # 注册
├── UserProfileActivity         # 用户信息
├── SongDetailActivity          # 歌曲详情/播放页
├── PlaylistDetailActivity      # 歌单详情
├── SearchActivity              # 搜索
├── MVPlayerActivity            # MV 播放
├── CommentActivity             # 评论列表
├── SettingsActivity            # 设置（含深色模式切换）
└── AboutActivity               # 关于
```

### 6.3 Intent 编程应用场景

学生需要掌握的 Intent 技术，贯穿整个应用的页面跳转和数据传递：

| 场景 | Intent 类型 | 示例 |
|------|-------------|------|
| **页面跳转传参** | 显式 Intent + Bundle | `Intent(this, SongDetailActivity::class.java).putExtra("songId", id)` |
| **启动服务** | 显式 Intent | `Intent(this, MusicPlayerService::class.java)` → `startService()` |
| **打开外部链接** | 隐式 Intent | `Intent(Intent.ACTION_VIEW, Uri.parse(url))` |
| **分享功能** | 隐式 Intent | `Intent(Intent.ACTION_SEND).setType("text/plain")` |
| **系统功能调用** | 隐式 Intent | `Intent(MediaStore.ACTION_MUSIC_PLAYER)` |
| **Activity 返回结果** | `startActivityForResult` | 从文件选择器选歌，回传结果 |
| **Intent Filter** | 隐式 Intent 注册 | 在 `AndroidManifest.xml` 中声明 Activity 可处理的 Action |

### 6.4 深色模式 / 主题切换

使用传统 Android 主题方案实现：

**方案：AppCompat.DayNight 主题**
```xml
<!-- res/values/themes.xml — 亮色主题 -->
<style name="Theme.NetMusic" parent="Theme.AppCompat.DayNight.NoActionBar">
    <item name="colorPrimary">@color/primary</item>
    <item name="colorPrimaryDark">@color/primary_dark</item>
    <item name="colorAccent">@color/accent</item>
</style>
```

```xml
<!-- res/values-night/themes.xml — 暗色主题（自动生效） -->
<style name="Theme.NetMusic" parent="Theme.AppCompat.DayNight.NoActionBar">
    <!-- 暗色配色的 colorPrimary 等 -->
</style>
```

**手动切换实现：**
1. 将用户的选择存入 `SharedPreferences`
2. 在 `Application` 或 `BaseActivity` 中读取偏好，调用 `AppCompatDelegate.setDefaultNightMode()`
3. 切换后通过 `recreate()` 刷新当前 Activity

```kotlin
// 切换深色模式
when (AppCompatDelegate.getDefaultNightMode()) {
    AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
    else -> AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
}
```

### 6.5 网络层

- 使用 **Retrofit** + **OkHttp** 进行 HTTP 通信
- 与后端 REST API 通过 JSON 交互
- 统一在 `com.example.netmusicandroid.network` 包中管理

---

## 七、附录

### 所用技术栈汇总
| 端 | 技术 |
|----|------|
| Android | Kotlin, 传统 Activity + XML 布局, AppCompat, Retrofit + OkHttp |
| 服务端 | Node.js, TypeScript, Express/Fastify, Prisma/TypeORM |
| 数据库 | MySQL / PostgreSQL |
| 鉴权 | JWT |
| Intent 编程 | 显式 Intent（页面跳转传参）、隐式 Intent（系统功能/分享/外部链接）|
| 文档 | Markdown + 可视化工具 |
