# NetMusic Server

NetMusic 音乐应用后端服务（Node.js + TypeScript + Express + Prisma 7 + libSQL）

---

## 目录

- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库操作](#数据库操作)
- [接口列表](#接口列表)
- [接口测试](#接口测试)
- [项目结构](#项目结构)
- [预置数据](#预置数据)
- [常用命令](#常用命令)
- [技术选型](#技术选型)

---

## 环境要求

- **Node.js** >= 18
- **npm** >= 9

---

## 快速开始

```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env：设置 DATABASE_URL="file:./dev.db"（默认即可）

# 2. 安装依赖
npm install

# 3. 生成 Prisma Client + 初始化数据库
npx prisma generate
npx prisma migrate dev --name init

# 4. 播种测试数据（可选）
npx prisma db seed

# 5. 启动开发服务器
npm run dev
```

服务启动后：

| 地址 | 说明 |
|------|------|
| `http://localhost:3000/api/v1` | API 根路径 |
| `http://localhost:3000/api-docs` | Swagger UI 接口文档 |
| `http://localhost:3000/api-docs.json` | OpenAPI JSON |
| `http://localhost:3000/static/` | 静态文件（头像、封面、歌曲） |

---

## 数据库操作

本项目使用 **Prisma 7** + **SQLite（libSQL 适配器）**。Prisma 7 的配置集中在 `prisma.config.ts` 中，包括 schema 路径、迁移目录、种子脚本和 `DATABASE_URL`。

### 初始化数据库

首次搭建环境时，执行以下命令：

```bash
# 生成 Prisma Client（类型定义）
npx prisma generate

# 创建初始迁移 + 建表
npx prisma migrate dev --name init

# 播种测试数据
npx prisma db seed
```

> `migrate dev --name init` 仅在数据库尚无迁移记录时使用。如果已有迁移，直接 `npx prisma migrate dev` 即可。

### 修改数据模型后

当你修改了 `prisma/schema.prisma` 中的模型定义：

```bash
# 自动生成迁移文件并应用到数据库
npx prisma migrate dev --name 你的改动描述

# 例子
npx prisma migrate dev --name add_favorite_table
```

### 重置数据库

想一键回到初始状态（删除数据库 → 重建表）：

```bash
npx prisma migrate reset

# Prisma 7 不会自动播种，需手动执行
npx prisma db seed
```

### 重新播种

只清空数据并重新插入测试数据（不改表结构）：

```bash
npx prisma db seed
```

### Prisma 客户端

Prisma Client 生成到 `src/generated/prisma/` 目录（在 `schema.prisma` 中通过 `output` 字段配置）。通常 `prisma migrate dev` 会自动重新生成，如需手动生成：

```bash
npx prisma generate
```

### 数据库 GUI

```bash
npx prisma studio
```

浏览器打开 Prisma Studio，可视化浏览和编辑数据。

---

## 接口列表

所有接口挂载在 `/api/v1` 下：

| 方法 | 路径 | 说明 | 鉴权 |
|------|------|------|------|
| POST | `/auth/register` | 注册 | 无 |
| POST | `/auth/login` | 登录（邮箱+密码） | 无 |
| POST | `/auth/refresh` | 刷新 Access Token | 无 |
| GET | `/users/me` | 获取当前用户信息 | Bearer Token |
| GET | `/users/:userId` | 获取用户公开信息 | 无 |
| PATCH | `/users/me` | 修改用户信息 | Bearer Token |
| PUT | `/users/me/avatar` | 上传/替换头像 (multipart) | Bearer Token |
| GET | `/users/me/playlists` | 获取我的歌单列表 | Bearer Token |
| GET | `/users/:userId/playlists` | 获取指定用户的歌单列表 | 无 |
| GET | `/songs` | 歌曲列表（分页，支持按歌手筛选） | 无 |
| GET | `/songs/:songId` | 歌曲详情 | 无 |
| GET | `/songs/:songId/comments` | 歌曲评论列表（分页） | 无 |
| POST | `/songs/:songId/comments` | 发表评论 | Bearer Token |
| DELETE | `/songs/:songId/comments/:commentId` | 删除评论（仅作者） | Bearer Token |
| GET | `/singers` | 歌手列表（分页） | 无 |
| GET | `/singers/:singerId` | 歌手详情（含歌曲列表） | 无 |
| GET | `/playlists/:playlistId` | 歌单详情（含歌曲列表） | 无 |
| POST | `/playlists` | 创建歌单 | Bearer Token |
| PATCH | `/playlists/:playlistId` | 重命名歌单 | Bearer Token |
| DELETE | `/playlists/:playlistId` | 删除歌单 | Bearer Token |
| POST | `/playlists/:playlistId/songs` | 歌单添加歌曲 | Bearer Token |
| DELETE | `/playlists/:playlistId/songs/:songId` | 歌单移除歌曲 | Bearer Token |
| GET | `/search/songs` | 搜索歌曲 | 无 |
| GET | `/search/singers` | 搜索歌手 | 无 |
| GET | `/search/playlists` | 搜索歌单 | 无 |

> 鉴权方式：Bearer Token — 在请求头添加 `Authorization: Bearer <token>`（登录后获取）。

---

## 接口测试

### 使用 Curl

确保服务器已启动（`npm run dev`），在新终端中执行：

```bash
# 注册
curl -s -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"pass123","email":"new@test.com"}'

# 登录（可使用预置账户，见下方列表）
curl -s -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"alice123"}'

# 测试限流：快速循环 12 次，第 11 次开始返回 429
for i in $(seq 1 12); do
  echo "=== Request $i ==="
  curl -s -X POST http://localhost:3000/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"ratelimit$i\",\"password\":\"pass123\",\"email\":\"rl$i@test.com\"}"
  echo ""
done
```

### 使用 Postman

1. 新建请求
2. Method 选择 **POST**
3. URL 填入 `http://localhost:3000/api/v1/auth/register`
4. Headers 添加 `Content-Type: application/json`
5. Body → raw → JSON：
   ```json
   {
     "username": "postmanuser",
     "password": "pass123",
     "email": "postman@test.com"
   }
   ```
6. 点击 Send

> **注意**：登录也是 POST，不是 GET。用 GET 请求会返回 `404 接口不存在`。

### 常见错误

| 现象 | 原因 | 解决 |
|------|------|------|
| `404 接口不存在` | 用 GET 请求了 POST 接口 | 改成 POST |
| `ECONNREFUSED` | 服务器没启动 | 先运行 `npm run dev` |
| `用户名已被占用` | 该用户名或邮箱已存在 | 换一个，或 `npx prisma db seed` 重置 |
| `请求过于频繁` | 15 分钟内请求超过 10 次 | 等 15 分钟，或重启服务器 |

---

## 项目结构

```
server/
├── prisma/
│   ├── schema.prisma              # 数据模型（6 个模型）
│   ├── seed.ts                    # 测试数据播种脚本
│   └── migrations/                # 数据库迁移历史
│
├── src/
│   ├── app.ts                     # Express 应用（中间件、路由、静态文件、Swagger）
│   ├── index.ts                   # 入口：启动 HTTP 服务器
│   ├── config/
│   │   ├── index.ts               # 环境变量配置
│   │   ├── database.ts            # PrismaClient 单例（libSQL 适配器）
│   │   └── openapi.ts             # OpenAPI / Swagger 配置
│   ├── routes/
│   │   ├── index.ts               # 路由聚合（挂载到 /api/v1）
│   │   ├── auth.routes.ts         # 认证路由
│   │   ├── user.routes.ts         # 用户路由（含头像上传、歌单子资源）
│   │   ├── singer.routes.ts       # 歌手路由
│   │   ├── song.routes.ts         # 歌曲路由（含评论子资源）
│   │   ├── playlist.routes.ts     # 歌单路由
│   │   └── search.routes.ts       # 搜索路由
│   ├── controllers/
│   │   ├── auth.controller.ts     # 注册 / 登录 / Token 刷新
│   │   ├── user.controller.ts     # 用户信息 / 头像上传
│   │   ├── singer.controller.ts   # 歌手列表 / 详情
│   │   ├── song.controller.ts     # 歌曲列表 / 详情 / 评论
│   │   ├── playlist.controller.ts # 歌单 CRUD / 歌曲管理
│   │   └── search.controller.ts   # 综合搜索
│   ├── services/
│   │   ├── auth.service.ts        # 注册、登录、Token 验证逻辑
│   │   ├── user.service.ts        # 用户资料读写
│   │   ├── singer.service.ts      # 歌手业务逻辑
│   │   ├── song.service.ts        # 歌曲业务逻辑
│   │   ├── playlist.service.ts    # 歌单业务逻辑
│   │   └── search.service.ts      # 搜索业务逻辑
│   ├── validators/
│   │   ├── auth.validator.ts      # registerSchema / loginSchema
│   │   ├── user.validator.ts      # updateUserSchema
│   │   ├── singer.validator.ts    # createSingerSchema / updateSingerSchema
│   │   ├── song.validator.ts      # createSongSchema / updateSongSchema
│   │   ├── playlist.validator.ts  # createPlaylistSchema / updatePlaylistSchema
│   │   └── search.validator.ts    # searchSchema
│   ├── docs/
│   │   ├── index.ts               # OpenAPI 文档聚合
│   │   ├── auth.docs.ts           # 认证接口文档
│   │   ├── user.docs.ts           # 用户接口文档
│   │   ├── singer.docs.ts         # 歌手接口文档
│   │   ├── song.docs.ts           # 歌曲接口文档
│   │   ├── playlist.docs.ts       # 歌单接口文档
│   │   └── search.docs.ts         # 搜索接口文档
│   ├── middlewares/
│   │   ├── auth.middleware.ts      # JWT 鉴权 → req.userId
│   │   ├── role.middleware.ts      # 角色鉴权（USER / ARTIST）
│   │   ├── error.middleware.ts     # 全局错误处理 + 404 处理
│   │   ├── logger.middleware.ts    # 请求日志
│   │   ├── rateLimiter.middleware.ts # 限流
│   │   ├── upload.middleware.ts    # Multer 文件上传配置
│   │   └── validate.ts            # Zod 校验中间件
│   ├── constants/
│   │   └── errorString.ts         # 用户提示文案
│   ├── errors/
│   │   └── AppError.ts            # 错误类（AppError 基类 + 5 子类）
│   └── utils/
│       ├── asyncHandler.ts        # 异步路由包装（自动 catch → next）
│       ├── jwt.ts                 # JWT 签发 / 验证
│       ├── password.ts            # bcrypt 密码哈希 / 比较
│       ├── response.ts            # 统一 JSON 响应 { code, message, data }
│       └── sanitize.ts            # XSS 过滤
│
├── storage/                       # 上传文件存储目录
│   ├── avatars/
│   ├── covers/
│   └── songs/
│
├── prisma.config.ts               # Prisma 7 配置（datasource、迁移、种子）
├── .env.example                   # 环境变量模板
├── package.json
└── README.md
```

---

## 预置数据

运行 `npx prisma db seed` 后，以下数据可用于测试：

### 用户

| 用户名 | 密码 | 角色 | 邮箱 | 签名 |
|--------|------|------|------|------|
| `alice` | `alice123` | USER | alice@example.com | 欢迎来到音乐世界 🎵 |
| `bob` | `bob123456` | ARTIST | bob@example.com | 摇滚不死 |
| `charlie` | `charlie123` | USER | charlie@example.com | 民谣爱好者 |
| `admin` | `admin123` | ARTIST | admin@netmusic.com | 系统管理员 |

### 歌手

| 歌手 | 描述 |
|------|------|
| Edvard Grieg | 挪威浪漫主义作曲家 |
| Rick Astley | 80 年代英伦流行 / 蓝眼灵魂 |

### 歌曲

| 歌曲 | 歌手 | 时长 |
|------|------|------|
| Anitra's Dance | Edvard Grieg | 3:19 |
| Never Gonna Give You Up | Rick Astley | 3:34 |

### 歌单

| 歌单名 | 所属用户 |
|--------|----------|
| 我最喜欢的歌 | alice |
| 摇滚精选 | bob |

### 评论

| 评论内容 | 歌曲 | 用户 |
|----------|------|------|
| 这首歌太好听了！ | Anitra's Dance | alice |
| Classic! | Never Gonna Give You Up | bob |

---

## 常用命令

```bash
npm run dev                 # 启动开发服务器（热重载）
npm run build               # TypeScript 编译
npm start                   # 启动生产构建
npm run lint                # ESLint 代码检查
npm test                    # 运行测试（Jest）
npx prisma studio           # Prisma 数据库 GUI
npx prisma migrate dev      # 同步 schema → 数据库（新项目加 --name init）
npx prisma db seed          # 播种测试数据
npx prisma migrate reset    # 重置数据库（手动 db seed 播种）
npx prisma generate         # 手动重新生成 Prisma Client
npx tsc --noEmit            # TypeScript 类型检查（不输出文件）
```

---

## 技术选型

| 类别 | 技术 |
|------|------|
| 运行时 | Node.js + TypeScript 6 |
| Web 框架 | Express 5 |
| 数据库 | SQLite（libSQL 适配器） |
| ORM | Prisma 7 |
| 校验 | Zod 4 |
| 认证 | JWT（jsonwebtoken） |
| 密码加密 | bcryptjs |
| 文件上传 | Multer 2 |
| 限流 | express-rate-limit |
| API 文档 | Swagger UI + OpenAPI |
| XSS 防护 | xss |
| 测试 | Jest + Supertest |
