# NetMusic Android

NetMusic 音乐应用 Android 客户端（Kotlin + MVVM + Retrofit + Room + ViewBinding）。

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
- [相关文档](#相关文档)

---

## 环境要求

| 工具 | 版本要求 |
|------|----------|
| **Android Studio** | Ladybug (2024.2.1) 或更高 |
| **JDK** | 11 或更高 |
| **Gradle** | 项目自带 Gradle Wrapper，无需手动安装 |
| **Android SDK** | Compile SDK 37, Min SDK 24, Target SDK 36 |
| **后端服务** | 需先启动 `../server/` 下的 Node.js 后端（见 [../server/README.md](../server/README.md)） |

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
npx prisma db push
npx prisma db seed
npm run dev
# 后端运行在 http://localhost:3000

# 3. 用 Android Studio 打开项目根目录 AndroidStudioProjects/

# 4. 同步 Gradle 依赖（Android Studio 会自动提示）

# 5. 如使用真机调试，修改 API 地址（见下方 [API 配置](#api-配置)）

# 6. 运行 app 模块到模拟器或真机
```

> **注意**：模拟器默认通过 `10.0.2.2:3000` 访问主机的 `localhost:3000`，无需额外配置。真机调试需将 `ApiConst.BASE_URL` 改为电脑的局域网 IP。

---

## 项目结构

```
app/
├── build.gradle.kts                  # app 模块构建配置
├── CLAUDE.md                         # Claude Code 开发指引
│
└── src/main/
    ├── AndroidManifest.xml           # Activity 注册 + 权限声明
    │
    ├── java/com/example/netmusicandroid/
    │   ├── MinMusicApp.kt            # Application 入口，初始化全局单例
    │   │
    │   ├── activity/                 # 独立页面 Activity
    │   │   ├── BaseActivity.kt       # 主容器：BottomNavigationView（首页/播放器/我的）
    │   │   ├── LoginActivity.kt      # 启动页：登录
    │   │   ├── RegisterActivity.kt   # 注册页
    │   │   ├── PlaylistActivity.kt   # 歌单浏览
    │   │   ├── PlaylistDetailActivity.kt  # 歌单详情 + 歌曲列表
    │   │   ├── SingerActivity.kt     # 歌手详情 + 歌曲列表
    │   │   ├── SingerListActivity.kt # 歌手列表
    │   │   ├── CommentActivity.kt    # 歌曲评论列表
    │   │   ├── MycommentActivity.kt  # 我的评论
    │   │   ├── CurrentPlaylistActivity.kt # 当前播放队列
    │   │   ├── RecentPlayActivity.kt # 最近播放
    │   │   ├── FavoritesActivity.kt  # 我的收藏
    │   │   ├── MySongsActivity.kt    # 我发布的歌曲
    │   │   ├── MoreSongActivity.kt   # 更多歌曲
    │   │   ├── SearchActivity.kt     # 搜索页
    │   │   ├── SettingActivity.kt    # 设置页
    │   │   └── UploadSongActivity.kt # 歌手上架歌曲
    │   │
    │   ├── fragment/                 # 3 个主 Fragment
    │   │   ├── HomeFragment.kt       # 首页：推荐歌曲 / 歌手列表 / 推荐歌单
    │   │   ├── PlayerFragment.kt     # 全屏播放器
    │   │   └── MineFragment.kt       # 我的：个人信息 / 收藏统计 / 评论统计 / 功能入口
    │   │
    │   ├── viewmodel/                # 页面状态与业务编排
    │   │   ├── BottomPlayerViewModel.kt  # 核心播放 ViewModel（BaseActivity 作用域共享）
    │   │   ├── LoginViewModel.kt / RegisterViewModel.kt
    │   │   ├── MineViewModel.kt / SettingViewModel.kt
    │   │   ├── CommentViewModel.kt / MyCommentViewModel.kt
    │   │   ├── FavoriteViewModel.kt / RecentPlayViewModel.kt / CurrentPlaylistViewModel.kt
    │   │   ├── PlaylistDetailViewModel.kt / UserPlaylistViewModel.kt
    │   │   ├── SingerViewModel.kt / SingerListViewModel.kt
    │   │   ├── SearchViewModel.kt / MoreSongViewModel.kt
    │   │   └── MySongsViewModel.kt
    │   │
    │   ├── data/
    │   │   ├── api/                  # Retrofit 接口定义
    │   │   │   ├── ApiClient.kt      # Retrofit 单例 + OkHttp Token 拦截器
    │   │   │   ├── AuthApiService.kt # 认证 / 用户接口
    │   │   │   ├── SongApiService.kt # 歌曲 / 评论 / 收藏接口
    │   │   │   ├── PlaylistApiService.kt
    │   │   │   ├── PlaylistDetailApiService.kt
    │   │   │   ├── SingerApiService.kt
    │   │   │   └── SearchApiService.kt
    │   │   │
    │   │   ├── db/                   # Room 本地数据库（DAO / Entity 同目录）
    │   │   │   ├── AppDatabase.kt
    │   │   │   ├── UserEntity.kt / UserDao.kt
    │   │   │   ├── PlayQueueEntity.kt / PlayQueueDao.kt
    │   │   │   └── RecentPlayEntity.kt / RecentPlayDao.kt
    │   │   │
    │   │   ├── model/                # API 响应与页面数据模型
    │   │   │   ├── ApiResponse.kt    # 统一响应：{ code, message, data }
    │   │   │   ├── SongItem.kt / SongDetail.kt
    │   │   │   ├── Playlist.kt / Singer.kt / CommentItem.kt
    │   │   │   └── ...
    │   │   │
    │   │   └── repository/           # 数据仓库
    │   │       ├── AuthRepository.kt       # 认证 + 用户信息 + 本地用户计数更新
    │   │       ├── SongRepository.kt       # 歌曲 / 评论 / 收藏数据
    │   │       ├── PlaylistRepository.kt   # 歌单数据
    │   │       ├── PlaylistDetailRepository.kt
    │   │       ├── SingerRepository.kt
    │   │       ├── SearchRepository.kt
    │   │       ├── PlayQueueRepository.kt  # 播放队列（Room 持久化）
    │   │       ├── RecentPlayRepository.kt # 最近播放（Room 持久化）
    │   │       └── RepositoryErrorParser.kt
    │   │
    │   ├── adapter/                  # RecyclerView Adapter
    │   │   ├── HomeSongAdapter.kt / SongListAdapter.kt / SingerSongAdapter.kt
    │   │   ├── HomeSingerAdapter.kt / HomePlaylistAdapter.kt
    │   │   ├── SearchSongAdapter.kt / SearchSingerAdapter.kt
    │   │   ├── UserPlaylistAdapter.kt / CommentAdapter.kt
    │   │   └── HistoryAccountAdapter.kt
    │   │
    │   ├── dialog/                   # Dialog 组件
    │   │   ├── AddToPlaylistDialog.kt
    │   │   ├── CreatePlaylist.kt
    │   │   └── EditProfileDialog.kt
    │   │
    │   ├── constant/                 # 常量
    │   │   ├── ApiConst.kt           # API 基础地址
    │   │   ├── DbConst.kt            # 数据库常量
    │   │   └── SpConst.kt            # SharedPreferences 键名
    │   │
    │   ├── sp/                       # SharedPreferences 管理
    │   │   └── SpManager.kt          # 登录状态 / 当前邮箱 / 用户 ID
    │   │
    │   └── utils/                    # 工具类
    │       ├── MusicPlayerManager.kt # MediaPlayer 封装（多监听者模式 + URL 解析）
    │       ├── ImageLoadUtil.kt      # Glide 图片加载工具
    │       ├── ToastUtil.kt          # Toast 工具
    │       └── SpUtil.kt             # SharedPreferences 工具（旧版，新代码优先用 SpManager）
    │
    └── res/
        ├── layout/                   # XML 布局文件
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

`MinMusicApp.onCreate()` 中初始化以下核心单例：

| 单例 | 用途 |
|------|------|
| `AppDatabase` | Room 数据库，提供 `globalUserDao` / `globalPlayQueueDao` / `globalRecentPlayDao` |
| `AuthRepository` | 登录 / 注册 / 登出 / Token 刷新 / 用户信息 / 头像上传 / 本地用户计数更新 |
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

**`BottomPlayerViewModel`** 是 BaseActivity 作用域内共享的播放 ViewModel，主 Fragment 通过 `activityViewModels()` 共享同一实例。播放队列支持首尾循环（最后一首 → 第一首，第一首 → 最后一首）。

### Token 认证流程

```
登录成功 → 存储 accessToken + refreshToken 到 Room(UserEntity)
         → OkHttp 拦截器自动在请求头添加 Authorization: Bearer <accessToken>

请求返回 401 → 拦截器自动调用 /auth/refresh 刷新 Token
            → 刷新成功：更新 Room 中的 Token，重试原请求
            → 刷新失败：清除登录状态，跳转登录页
```

`SpManager` 只保存登录状态、当前邮箱和用户 ID；Token 以 Room 为准。

### 我的页面计数

`MineFragment` 中的收藏数量、我的评论数量来自当前登录用户的 Room `UserEntity.favoriteCount` / `commentCount`。收藏、取消收藏、发表评论、删除评论等操作在服务端成功后，会通过 `AuthRepository.updateCurrentUserFavoriteCount(delta)` 或 `updateCurrentUserCommentCount(delta)` 更新本地计数，Room Flow 自动通知 UI 刷新，因此不需要为了刷新数量额外请求一次个人信息。

---

## 功能模块

### 1. 用户认证
- 邮箱 + 密码注册 / 登录
- JWT 双 Token 机制（accessToken + refreshToken）
- Token 自动刷新，刷新失败自动登出
- 历史账号管理（Room 持久化）

### 2. 首页
- 推荐歌曲（横向滚动列表）
- 歌手列表（横向滚动）
- 推荐歌单（网格展示）
- 下拉刷新
- 底部迷你播放条（点击展开全屏播放器）

### 3. 音乐播放
- 全屏播放器：封面、歌名、歌手、进度条、总时长 / 当前时长
- 播放控制：播放 / 暂停、上一首 / 下一首
- 收藏 / 取消收藏切换
- 添加歌曲到歌单
- 查看歌曲评论
- 迷你播放条（首页、我的页面常驻）
- 当前播放队列（Room 持久化，支持首尾循环）
- 最近播放记录

### 4. 歌手浏览
- 全部歌手列表
- 歌手详情页（歌手信息 + 歌曲列表）
- 按歌手筛选歌曲

### 5. 歌单管理
- 创建歌单
- 删除歌单
- 歌单添加 / 移除歌曲
- 歌单详情页（歌单信息 + 歌曲列表）
- 收藏歌单（注册时自动创建，用于保存收藏歌曲）

### 6. 评论
- 查看歌曲评论（分页）
- 发表评论
- 删除自己的评论
- 我的评论列表
- 评论数量本地同步到我的页面

### 7. 搜索
- 搜索歌曲
- 搜索歌手
- 搜索歌单
- 分页加载

### 8. 个人中心（我的）
- 查看 / 编辑个人信息（昵称、签名）
- 头像上传
- 收藏数量、我的评论数量展示
- 当前播放、最近播放、我的歌单、我的收藏、我的评论入口
- 我发布的歌曲 / 上架歌曲（ARTIST 角色相关入口）
- 设置、帮助与反馈入口

### 9. 歌曲上架（ARTIST 角色）
- 上传歌曲（音频文件 + 可选封面图片 + 歌曲名）
- 查看自己发布的歌曲
- 下架自己的歌曲

---

## API 配置

API 基础地址定义在 `constant/ApiConst.kt`：

```kotlin
object ApiConst {
    const val PLAYLIST_DETAIL = "playlists/"
    const val BASE_URL = "http://10.0.2.2:3000/api/v1/"
}
```

| 地址 | 说明 |
|------|------|
| `http://10.0.2.2:3000/api/v1/` | 模拟器访问主机 localhost 的别名（默认） |
| `http://<你的局域网IP>:3000/api/v1/` | 真机调试时使用 |

静态资源（歌曲文件、封面、头像）与 API 使用同一后端主机。客户端通过 `MusicPlayerManager.resolveUrl(path)` 将接口返回的相对路径转换为完整 URL：先从 `ApiConst.BASE_URL` 去掉 `/api/v1/` 得到主机地址，再拼接资源路径。

> 模拟器通过 `10.0.2.2` 自动映射到宿主机的 `localhost`，因此默认配置无需修改即可使用。

---

## 常用命令

所有 Gradle 命令在项目根目录 `AndroidStudioProjects/` 下执行；如果当前目录是 `app/`，可先 `cd ..`。

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 只编译 app Kotlin 代码
./gradlew :app:compileDebugKotlin

# 编译 Release APK
./gradlew assembleRelease

# 运行单元测试（JUnit 4，本地 JVM）
./gradlew test

# 运行插桩测试（需要模拟器或真机）
./gradlew connectedAndroidTest

# 清理构建产物
./gradlew clean

# 查看 app 依赖树
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
| 网络请求 | Retrofit + OkHttp + Gson |
| 本地数据库 | Room + KSP |
| 图片加载 | Glide |
| 异步处理 | Kotlin Coroutines |
| UI 框架 | AndroidX + Material Components + ViewBinding |
| 构建系统 | Gradle (Kotlin DSL) + Version Catalog |
| 编译 SDK | 37 |
| 最低支持 | Android 7.0 (API 24) |
| 目标 SDK | 36 |
| Java 兼容 | 11 |

---

## 相关文档

- [后端服务文档](../server/README.md) — Node.js/TypeScript 后端 API 与环境说明
- [系统总览](../README.md) — 整体项目介绍
- [CLAUDE.md](./CLAUDE.md) — Claude Code 开发指引
