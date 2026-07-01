# NetMusic — 全栈音乐流媒体应用

一个完整的音乐流媒体应用，包含 Android 客户端（Kotlin）和 Node.js 后端服务（TypeScript + Express + Prisma）。

---

## 目录

- [项目简介](#项目简介)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [功能概览](#功能概览)
- [项目结构](#项目结构)
- [技术栈总览](#技术栈总览)
- [相关文档](#相关文档)

---

## 项目简介

NetMusic 是一个功能完整的音乐流媒体平台，用户可以注册登录、浏览歌曲和歌手、创建管理歌单、发表评论、搜索内容。支持普通用户（USER）和艺术家（ARTIST）两种角色，艺术家可以上传和管理自己的歌曲。

**核心特性：**

- 🎵 在线音乐播放（全屏播放器 + 迷你播放条）
- 🔐 JWT 双 Token 认证（accessToken 15min + refreshToken 7天）
- 📋 歌单创建、编辑、管理
- 💬 歌曲评论系统
- 🔍 歌曲 / 歌手 / 歌单搜索
- 👤 用户个人中心（头像、昵称、签名）
- 🎤 ARTIST 角色可上架 / 下架歌曲
- 📱 Android 原生客户端（MVVM 架构）
- 🚀 Node.js RESTful 后端（Express 5 + Prisma 7）
- 📖 Swagger UI 在线接口文档

---

## 系统架构

```
┌──────────────────────────────────────────────────────────┐
│                    Android Client (Kotlin)                │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │  HomeFragment │PlayerFragment│ MineFragment │         │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘               │
│       │              │              │                     │
│  ┌────▼──────────────▼──────────────▼─────┐              │
│  │        BottomPlayerViewModel           │              │
│  │    (全局共享播放状态 + 播放控制)         │              │
│  └────────────────┬───────────────────────┘              │
│                   │                                       │
│  ┌────────────────▼───────────────────────┐              │
│  │           Repository 层                 │              │
│  │  AuthRepo │ SongRepo │ PlaylistRepo ... │              │
│  └────┬──────────┬──────────┬─────────────┘              │
│       │          │          │                             │
│  ┌────▼────┐ ┌───▼────┐ ┌──▼──────────┐                 │
│  │ Retrofit│ │  Room  │ │MusicPlayer  │                 │
│  │ + OkHttp│ │  (SQL) │ │  Manager    │                 │
│  └────┬────┘ └────────┘ └─────────────┘                 │
│       │                                                   │
└───────┼───────────────────────────────────────────────────┘
        │  HTTP REST (JSON)
        │  Static Files (songs/covers/avatars)
        ▼
┌──────────────────────────────────────────────────────────┐
│                  Node.js Backend (TypeScript)             │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐             │
│  │  Express 5│  │  Multer  │  │ Rate Limit │             │
│  │  Routes   │  │ (upload) │  │  (安全)    │             │
│  └────┬─────┘  └──────────┘  └────────────┘             │
│       │                                                   │
│  ┌────▼─────────────────────────────────────┐            │
│  │  Middleware Stack                         │            │
│  │  Auth (JWT) │ Role │ Validate (Zod) │ Error│           │
│  └────┬─────────────────────────────────────┘            │
│       │                                                   │
│  ┌────▼────────┐  ┌──────────┐  ┌──────────┐            │
│  │ Controllers │  │ Services │  │Validators│            │
│  └────┬────────┘  └────┬─────┘  └──────────┘            │
│       │                │                                  │
│  ┌────▼────────────────▼─────┐                           │
│  │    Prisma 7 + libSQL      │                           │
│  │    (SQLite 数据库)         │                           │
│  └───────────────────────────┘                           │
│                                                           │
│  ┌───────────────────────────┐                           │
│  │  Static File Storage      │                           │
│  │  avatars/ covers/ songs/  │                           │
│  └───────────────────────────┘                           │
└──────────────────────────────────────────────────────────┘
```

**数据流简述：**

1. Android 客户端通过 Retrofit 向 `/api/v1/*` 发送 HTTP 请求
2. Express 路由层经过 Auth（JWT 鉴权）→ Validate（Zod 校验）→ Controller → Service 处理
3. Service 层通过 Prisma ORM 操作 SQLite 数据库
4. 文件上传（头像、歌曲、封面）通过 Multer 中间件处理，存入 `static/` 目录
5. 统一 JSON 响应格式：`{ code: Int, message: String?, data: T? }`
6. 客户端 Repository 层返回 `Result<T>`，ViewModel 暴露 LiveData / Flow 给 UI

---

## 快速开始

### 1. 启动后端

```bash
cd server

# 安装依赖
npm install

# 配置环境变量
cp .env.example .env

# 初始化数据库
npx prisma generate
npx prisma migrate dev --name init
npx prisma db seed    # 播种测试数据

# 启动服务
npm run dev
# → API:     http://localhost:3000/api/v1
# → Swagger: http://localhost:3000/api-docs
```

### 2. 启动 Android 客户端

```bash
# 用 Android Studio 打开项目根目录
# 同步 Gradle → 选择 app 模块 → Run 到模拟器或真机

# 或命令行编译：
./gradlew assembleDebug
```

> 模拟器默认通过 `10.0.2.2:3000` 连接宿主机后端。真机需修改 `app/.../constant/ApiConst.kt` 中的 `BASE_URL` 为电脑局域网 IP。

### 3. 测试账户

启动后端并播种数据后，可使用以下预置账户登录：

| 用户名 | 邮箱 | 密码 | 角色 |
|--------|------|------|------|
| alice | alice@example.com | alice123 | USER |
| bob | bob@example.com | bob123456 | ARTIST |
| charlie | charlie@example.com | charlie123 | USER |
| admin | admin@netmusic.com | admin123 | ARTIST |

---

## 功能概览

| 功能 | 客户端 | 后端 |
|------|--------|------|
| 注册 / 登录 | ✅ LoginActivity / RegisterActivity | ✅ JWT 双 Token |
| Token 自动刷新 | ✅ OkHttp 拦截器 | ✅ /auth/refresh |
| 首页推荐 | ✅ HomeFragment（三栏布局） | ✅ 原子 API + 客户端聚合 |
| 音乐播放 | ✅ PlayerFragment + 迷你播放条 | ✅ 静态文件服务 |
| 播放队列 | ✅ Room 持久化 + 首尾循环 | — |
| 最近播放 | ✅ Room 持久化 | — |
| 歌手浏览 | ✅ SingerActivity / SingerDetailActivity | ✅ /singers 分页接口 |
| 歌单管理 | ✅ PlaylistActivity（CRUD） | ✅ /playlists 全套接口 |
| 收藏歌曲 | ✅ PlayerFragment 切换收藏 | ✅ toggle favorite |
| 评论系统 | ✅ CommentActivity | ✅ 发表 / 删除（仅作者） |
| 搜索 | ✅ SearchActivity | ✅ 歌曲 / 歌手 / 歌单搜索 |
| 个人中心 | ✅ MineFragment | ✅ /users/me 系列接口 |
| 头像上传 | ✅ EditProfileDialog | ✅ Multer multipart |
| 歌曲上架 | ✅ ARTIST 角色上传表单 | ✅ Multer + 歌曲元信息入库 |
| 接口文档 | — | ✅ Swagger UI |

---

## 项目结构

```
AndroidStudioProjects/
├── app/                         # Android 客户端（Kotlin）
│   ├── README.md                # 客户端文档
│   ├── CLAUDE.md                # Claude Code 开发指引
│   ├── build.gradle.kts         # 模块构建配置
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/netmusicandroid/
│       │   ├── activity/        # 18 个 Activity
│       │   ├── fragment/        # 3 个主 Fragment
│       │   ├── viewmodel/       # 16 个 ViewModel
│       │   ├── data/
│       │   │   ├── api/         # Retrofit 接口
│       │   │   ├── db/          # Room 数据库
│       │   │   ├── model/       # 数据模型
│       │   │   └── repository/  # 数据仓库
│       │   ├── adapter/         # RecyclerView 适配器
│       │   ├── utils/           # 工具类
│       │   └── ...
│       └── res/                 # 布局/资源文件
│
├── server/                      # Node.js 后端（TypeScript）
│   ├── README.md                # 后端文档
│   ├── CLAUDE.md                # Claude Code 开发指引
│   ├── prisma/
│   │   ├── schema.prisma        # 数据模型（6 模型 + 1 枚举）
│   │   └── seed.ts              # 测试数据播种
│   ├── src/
│   │   ├── routes/              # 6 个路由模块
│   │   ├── controllers/         # 6 个控制器
│   │   ├── services/            # 6 个服务
│   │   ├── validators/          # Zod 校验
│   │   ├── middlewares/         # Auth / Role / Error / Logger / RateLimit / Upload / Validate
│   │   ├── docs/                # OpenAPI 文档
│   │   └── utils/               # JWT / Password / Response / Sanitize
│   ├── static/                  # 静态文件（头像/封面/歌曲）
│   └── tests/                   # Jest + Supertest 测试
│
├── gradle/                      # Gradle Wrapper + Version Catalog
├── build.gradle.kts             # 根构建配置
├── settings.gradle.kts          # 项目设置
├── README.md                    # 本文件：系统总览
└── docs/                        # 项目文档
```

---

## 技术栈总览

### 客户端

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | — |
| 架构 | MVVM + Repository | — |
| 网络 | Retrofit + OkHttp + Gson | 3.0 / 5.4 |
| 数据库 | Room + KSP | 2.8 |
| 图片 | Glide | 4.16 |
| 异步 | Kotlin Coroutines | 1.7 |
| UI | AndroidX + Material + ViewBinding | — |
| 构建 | Gradle Kotlin DSL + Version Catalog | — |

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 运行时 | Node.js + TypeScript | ≥18 / 6 |
| 框架 | Express | 5 |
| 数据库 | SQLite（libSQL） | — |
| ORM | Prisma | 7 |
| 校验 | Zod | 4 |
| 认证 | JWT（jsonwebtoken） | — |
| 加密 | bcryptjs | — |
| 上传 | Multer | 2 |
| 文档 | Swagger UI + OpenAPI | — |
| 测试 | Jest + Supertest | — |

### 数据库模型

| 模型 | 说明 |
|------|------|
| **User** | 用户（id, username, password, email, avatar, signature, role） |
| **Singer** | 歌手（id, name, description, coverUrl, userId） |
| **Song** | 歌曲（id, name, duration, fileUrl, coverUrl, singerId） |
| **Playlist** | 歌单（id, name, userId, isDefault） |
| **PlaylistSong** | 歌单-歌曲关联（playlistId, songId） |
| **Comment** | 评论（id, content, userId, songId） |

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [app/README.md](app/README.md) | Android 客户端完整文档 |
| [server/README.md](server/README.md) | Node.js 后端完整文档（31 个 API 端点 + 测试指南） |
| [app/CLAUDE.md](app/CLAUDE.md) | 客户端 Claude Code 开发指引 |
| [server/CLAUDE.md](server/CLAUDE.md) | 后端 Claude Code 开发指引 |
| [server/docs/](server/docs/) | 后端详细设计文档（7 份，中文） |
