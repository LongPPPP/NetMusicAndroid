# NetMusic Android

NetMusic 音乐应用 Android 客户端（Kotlin + MVVM + Retrofit + Room）

---

## 目录

- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [架构设计](#架构设计)
- [功能模块](#功能模块)
- [API 配置](#api-配置)
- [常用命令](#常用命令)
- [技术选型](#技术选型)

---

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| **Android Studio** | Ladybug (2024.2.1) 或更高 |
| **JDK** | 11 或更高 |
| **Gradle** | 项目自带 Gradle Wrapper，无需手动安装 |
| **Android SDK** | Compile SDK 37, Min SDK 24, Target SDK 36 |
| **后端服务** | 需先启动 `server/` 下的 Node.js 后端（见 [../README.md](../README.md)） |

---

## 快速开始

```bash
# 1. 克隆项目
git clone <repo-url>
cd AndroidStudioProjects

# 2. 启动后端服务（新终端）
cd server
cp .env.example .env
npm install
npx prisma generate
npx prisma migrate dev --name init
npx prisma db seed
npm run dev
# 后端运行在 http://localhost:3000

# 3. 用 Android Studio 打开项目根目录 AndroidStudioProjects/

# 4. 同步 Gradle 依赖（Android Studio 会自动提示）

# 5. 如使用真机调试，修改 API 地址（见下方 [API 配置](#api-配置)）

# 6. 运行 app 模块到模拟器或真机
```

> **注意**：模拟器默认通过 `10.0.2.2:3000` 访问主机的 `localhost:3000`，无需额外配置。真机调试需修改为电脑的局域网 IP。

---

## 项目结构

```
app/
├── build.gradle.kts                  # 模块构建配置（AGP 9.2.1）
├── CLAUDE.md                         # Claude Code 开发指引
│
└── src/main/
    ├── AndroidManifest.xml           # 19 个 Activity 注册 + 权限声明
    │
    ├── java/com/example/netmusicandroid/
    │   ├── MinMusicApp.kt            # Application 入口，初始化全局单例
    │   │
    │   ├── activity/                 # 18 个 Activity
    │   │   ├── BaseActivity.kt       # 主容器：BottomNavigationView（首页/播放器/我的）
    │   │   ├── LoginActivity.kt      # 启动页：登录
    │   │   ├── RegisterActivity.kt   # 注册页
    │   │   ├── PlaylistActivity.kt   # 歌单浏览
    │   │   ├── PlaylistDetailActivity.kt  # 歌单详情 + 歌曲列表
    │   │   ├── SingerActivity.kt     # 歌手浏览
    │   │   ├── SingerDetailActivity.kt   # 歌手详情 + 歌曲列表
    │   │   ├── CommentActivity.kt    # 评论列表
    │   │   ├── SearchActivity.kt     # 搜索页
    │   │   ├── SettingActivity.kt    # 设置页
    │   │   ├── FavoritesActivity.kt  # 我的收藏
    │   │   └── ...                   # 更多 Activity
    │   │
    │   ├── fragment/                 # 3 个主 Fragment
    │   │   ├── HomeFragment.kt       # 首页：推荐歌曲 / 歌手列表 / 推荐歌单
    │   │   ├── PlayerFragment.kt     # 全屏播放器
    │   │   └── MineFragment.kt       # 我的：个人信息 / 上传歌曲 / 收藏 / 评论
    │   │
    │   ├── viewmodel/                # 16 个 ViewModel
    │   │   ├── BottomPlayerViewModel.kt  # 核心播放 ViewModel（全局共享）
    │   │   ├── AuthViewModel.kt
    │   │   ├── HomeViewModel.kt
    │   │   ├── SongViewModel.kt
    │   │   ├── PlaylistViewModel.kt
    │   │   ├── SingerViewModel.kt
    │   │   ├── CommentViewModel.kt
    │   │   ├── SearchViewModel.kt
    │   │   └── ...
    │   │
    │   ├── data/
    │   │   ├── api/                  # Retrofit 接口定义
    │   │   │   ├── ApiClient.kt      # Retrofit 单例 + OkHttp 拦截器
    │   │   │   ├── ApiAuthService.kt # 认证接口
    │   │   │   ├── ApiSongService.kt # 歌曲接口
    │   │   │   ├── ApiUserService.kt # 用户接口
    │   │   │   ├── ApiPlaylistService.kt  # 歌单接口
    │   │   │   ├── ApiSingerService.kt    # 歌手接口
    │   │   │   └── ApiSearchService.kt    # 搜索接口
    │   │   │
    │   │   ├── db/                   # Room 本地数据库
    │   │   │   ├── AppDatabase.kt    # Room 数据库定义
    │   │   │   ├── dao/              # 3 个 DAO（User / PlayQueue / RecentPlay）
    │   │   │   └── entity/           # 3 个 Entity
    │   │   │
    │   │   ├── model/                # 15 个数据模型类
    │   │   │   ├── ApiResponse.kt    # 统一响应：{ code, message, data }
    │   │   │   ├── Song.kt
    │   │   │   ├── SongDetail.kt
    │   │   │   ├── Singer.kt
    │   │   │   ├── Playlist.kt
    │   │   │   ├── Comment.kt
    │   │   │   └── ...
    │   │   │
    │   │   └── repository/           # 7 个 Repository
    │   │       ├── AuthRepository.kt       # 认证 + 用户信息
    │   │       ├── SongRepository.kt       # 歌曲数据
    │   │       ├── PlaylistRepository.kt   # 歌单数据
    │   │       ├── SingerRepository.kt     # 歌手数据
    │   │       ├── CommentRepository.kt    # 评论数据
    │   │       ├── SearchRepository.kt     # 搜索数据
    │   │       ├── PlayQueueRepository.kt  # 播放队列（Room 持久化）
    │   │       └── RecentPlayRepository.kt # 最近播放（Room 持久化）
    │   │
    │   ├── adapter/                  # 10 个 RecyclerView Adapter
    │   │   ├── SongAdapter.kt
    │   │   ├── SingerAdapter.kt
    │   │   ├── PlaylistAdapter.kt
    │   │   ├── CommentAdapter.kt
    │   │   └── ...
    │   │
    │   ├── dialog/                   # 3 个 Dialog 组件
    │   │   ├── EditProfileDialog.kt  # 编辑资料弹窗
    │   │   ├── CreatePlaylistDialog.kt
    │   │   └── ...
    │   │
    │   ├── constant/                 # 常量
    │   │   ├── ApiConst.kt           # API 地址 + 静态资源地址
    │   │   ├── DbConst.kt            # 数据库常量
    │   │   └── SpConst.kt            # SharedPreferences 键名
    │   │
    │   ├── sp/                       # SharedPreferences 管理
    │   │   └── SpManager.kt          # 登录状态 / 用户邮箱 / 用户 ID
    │   │
    │   └── utils/                    # 工具类
    │       ├── MusicPlayerManager.kt # MediaPlayer 封装（多监听者模式）
    │       ├── ImageLoadUtil.kt      # Glide 图片加载工具
    │       ├── ToastUtil.kt          # Toast 工具
    │       └── SpUtil.kt             # SharedPreferences 工具（旧版，新代码用 SpManager）
    │
    └── res/
        ├── layout/                   # 40+ XML 布局文件
        ├── drawable/                 # 图标、背景、矢量图
        ├── mipmap/                   # 应用图标
        ├── values/                   # 字符串、颜色、主题
        └── xml/                      # backup_rules / data_extraction_rules
```

---

## 架构设计

### MVVM + Repository 模式

```
Activity/Fragment → ViewModel → Repository → ApiService (Retrofit)
                                  ↓
                              Room DAO (本地缓存)
```

**分层职责：**

| 层级 | 职责 |
|------|------|
| **Activity / Fragment** | UI 展示，用户交互，通过 `activityViewModels()` 或 `viewModels()` 获取 ViewModel |
| **ViewModel** | 持有 UI 状态（LiveData / Flow），调用 Repository，暴露数据给 UI |
| **Repository** | 数据访问层，封装网络请求和本地数据库操作，返回 `Result<T>` |
| **ApiService** | Retrofit 接口定义，声明 API 端点、请求参数、响应类型 |

### 全局单例（Application 初始化）

`MinMusicApp.onCreate()` 中按顺序初始化以下核心单例：

| 单例 | 用途 |
|------|------|
| `AppDatabase` | Room 数据库，提供 `globalUserDao` / `globalPlayQueueDao` / `globalRecentPlayDao` |
| `AuthRepository` | 登录 / 注册 / 登出 / Token 刷新 / 用户信息 / 头像上传 |
| `PlayQueueRepository` | 播放队列持久化 |
| `RecentPlayRepository` | 最近播放记录持久化 |
| `ApiClient` | Retrofit + OkHttp（含 Token 自动刷新拦截器） |
| `SpManager` | SharedPreferences：登录状态、当前邮箱、用户 ID |
| `MusicPlayerManager` | MediaPlayer 封装：播放、暂停、切歌、进度控制 |

### 播放架构

```
用户点击歌曲
  → BottomPlayerViewModel.playSong(song)
    → PlayQueueRepository 写入播放队列
    → RecentPlayRepository 记录最近播放
    → MusicPlayerManager.play(url)
      → 多监听者回调通知所有订阅方

UI 更新（观察同一 ViewModel）：
  - PlayerFragment（全屏播放器）→ currentSong, isLiked
  - HomeFragment（迷你播放条）→ songName, singerName, coverUrl, isPlaying
  - MineFragment（迷你播放条）→ songName, singerName, coverUrl, isPlaying
```

**`BottomPlayerViewModel`** 是全局唯一的播放 ViewModel，所有 Fragment 通过 `activityViewModels()` 共享同一实例。播放队列支持首尾循环（最后一首 → 第一首，第一首 → 最后一首）。

### Token 认证流程

```
登录成功 → 存储 accessToken + refreshToken 到 Room
         → OkHttp 拦截器自动在请求头添加 Authorization: Bearer <accessToken>

请求返回 401 → 拦截器自动调用 /auth/refresh 刷新 Token
            → 刷新成功：更新 Room 中的 Token，重试原请求
            → 刷新失败：清除登录状态，跳转登录页
```

---

## 功能模块

### 1. 用户认证
- 邮箱 + 密码注册 / 登录
- JWT 双 Token 机制（accessToken 15min + refreshToken 7天）
- Token 自动刷新，401 自动登出
- 历史账号管理（Room 持久化）

### 2. 首页
- 推荐歌曲（横向滚动列表）
- 歌手列表（横向滚动）
- 推荐歌单（网格展示）
- 下拉刷新
- 底部迷你播放条（点击展开全屏播放器）

### 3. 音乐播放
- 全屏播放器：封面、歌名、歌手、进度条
- 播放控制：播放/暂停、上一首/下一首
- 收藏/取消收藏切换
- 迷你播放条（首页、我的页面常驻）
- 播放队列（Room 持久化，支持首尾循环）
- 最近播放记录

### 4. 歌手浏览
- 全部歌手列表（分页）
- 歌手详情页（歌手信息 + 歌曲列表）
- 按歌手筛选歌曲

### 5. 歌单管理
- 创建歌单
- 重命名歌单
- 删除歌单
- 歌单添加 / 移除歌曲
- 歌单详情页（歌单信息 + 歌曲列表）
- 收藏歌单（注册时自动创建，不可删除/改名）

### 6. 评论
- 查看歌曲评论（分页）
- 发表评论
- 删除自己的评论

### 7. 搜索
- 搜索歌曲
- 搜索歌手
- 搜索歌单
- 分页加载

### 8. 个人中心（我的）
- 查看 / 编辑个人信息（昵称、签名）
- 头像上传
- 我的上传（ARTIST 角色可见）
- 我的收藏
- 我的评论
- 最近播放
- 设置

### 9. 歌曲上架（ARTIST 角色）
- 上传歌曲（音频文件 + 可选封面图片 + 歌曲名）
- 下架自己的歌曲

---

## API 配置

API 基础地址定义在 `constant/ApiConst.kt`：

```kotlin
// 模拟器使用（默认）
const val BASE_URL = "http://10.0.2.2:3000/api/v1/"

// 真机调试使用（替换为电脑局域网 IP）
// const val BASE_URL = "http://192.168.x.x:3000/api/v1/"
```

| 地址 | 说明 |
|------|------|
| `http://10.0.2.2:3000/api/v1/` | 模拟器访问主机 localhost 的别名（默认） |
| `http://<你的局域网IP>:3000/api/v1/` | 真机调试时使用 |
| `http://10.0.2.2:3000/static/` | 静态资源（歌曲文件、封面、头像） |

> 模拟器通过 `10.0.2.2` 自动映射到宿主机的 `localhost`，因此默认配置无需修改即可使用。

---

## 常用命令

所有 Gradle 命令在项目根目录 `AndroidStudioProjects/` 下执行：

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 编译 Release APK
./gradlew assembleRelease

# 运行单元测试（JUnit 4，本地 JVM）
./gradlew test

# 运行插桩测试（需要模拟器或真机）
./gradlew connectedAndroidTest

# 清理构建产物
./gradlew clean

# 查看依赖树
./gradlew app:dependencies

# 启动后端服务
cd server && npm run dev
```

---

## 技术选型

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| 架构 | MVVM + Repository |
| 网络请求 | Retrofit 3 + OkHttp 5 + Gson |
| 本地数据库 | Room 2.8 + KSP |
| 图片加载 | Glide 4.16 |
| 异步处理 | Kotlin Coroutines 1.7 |
| UI 框架 | AndroidX + Material Components 1.14 + ViewBinding |
| 构建系统 | Gradle (Kotlin DSL) + Version Catalog |
| 编译 SDK | 37 |
| 最低支持 | Android 7.0 (API 24) |
| 目标 SDK | 36 (Android 15) |
| Java 兼容 | 11 |

---

## 相关文档

- [后端服务文档](../server/README.md) — Node.js 后端 API 完整文档
- [系统总览](../README.md) — 整体项目介绍
- [CLAUDE.md](./CLAUDE.md) — Claude Code 开发指引（英文）
