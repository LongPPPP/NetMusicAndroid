# NetMusic Server

NetMusic 音乐应用后端服务（Node.js + TypeScript + Express + Prisma）

---

## 目录

- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [数据库操作](#数据库操作)
- [接口测试](#接口测试)
- [项目结构](#项目结构)
- [预置用户](#预置用户)
- [常用命令](#常用命令)

---

## 环境要求

- **Node.js** >= 18
- **npm** >= 9

---

## 快速开始

```bash
# 1. 安装依赖
npm install

# 2. 初始化数据库（建表 + 灌测试数据）
npx prisma migrate dev
npx prisma db seed

# 3. 启动开发服务器
npm run dev
```

服务启动后访问 `http://localhost:3000/api/v1`。

---

## 数据库操作

### 初始化数据库

首次搭建环境时，两条命令依次执行：

```bash
# 根据 schema.prisma 创建/更新数据库表
npx prisma migrate dev

# 插入预置测试数据（4 个用户）
npx prisma db seed
```

### 修改数据模型后

当你修改了 `prisma/schema.prisma` 中的模型定义：

```bash
# 自动生成迁移文件并应用到数据库
npx prisma migrate dev --name 你的改动描述

# 例子
npx prisma migrate dev --name add_song_table
```

### 重置数据库

想一键回到初始状态（删除数据库 → 重建表 → 重新播种）：

```bash
npx prisma migrate reset
```

### 重新播种

只清空数据并重新插入测试数据（不改表结构）：

```bash
npx prisma db seed
```

### Prisma 客户端

修改 schema 后，如果需要手动重新生成 Prisma 客户端类型：

```bash
npx prisma generate
```

> 通常 `prisma migrate dev` 会自动执行 generate，不需要手动调用。

---

## 接口测试

### 使用 Curl

确保服务器已启动（`npm run dev`），在新终端中执行：

```bash
# 注册
curl -s -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","password":"pass123","email":"new@test.com"}'

# 登录（可用预置用户，见下方列表）
curl -s -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}'

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

| 现象             | 原因                | 解决                               |
|----------------|-------------------|----------------------------------|
| `404 接口不存在`    | 用 GET 请求了 POST 接口 | 改成 POST                          |
| `ECONNREFUSED` | 服务器没启动            | 先运行 `npm run dev`                |
| `用户名已被占用`      | 该用户名已存在           | 换个用户名，或用 `npx prisma db seed` 重置 |
| `请求过于频繁`       | 15 分钟内请求超过 10 次   | 等 15 分钟，或重启服务器                   |

---

## 项目结构

```
server/
├── prisma/
│   ├── schema.prisma          # 数据模型定义
│   ├── seed.ts                # 测试数据播种脚本
│   └── migrations/            # 数据库迁移历史
│
├── src/
│   ├── index.ts               # Express 应用入口
│   ├── config/
│   │   ├── index.ts           # 环境变量配置
│   │   └── database.ts        # Prisma 客户端
│   ├── routes/
│   │   ├── index.ts           # 路由聚合
│   │   └── auth.routes.ts     # 认证路由
│   ├── controllers/
│   │   └── auth.controller.ts # HTTP 层：参数校验、响应
│   ├── services/
│   │   └── auth.service.ts    # 业务逻辑层：注册/登录
│   ├── validators/
│   │   └── auth.validator.ts  # Zod schema 校验规则
│   ├── middlewares/
│   │   ├── auth.middleware.ts  # JWT 鉴权
│   │   ├── rateLimiter.middleware.ts
│   │   ├── error.middleware.ts # 全局错误处理
│   │   └── logger.middleware.ts
│   ├── constants/
│   │   └── errorString.ts     # 错误码枚举
│   └── utils/
│       ├── password.ts         # bcrypt 加密
│       ├── jwt.ts              # JWT 签发/验证
│       ├── sanitize.ts         # XSS 过滤
│       └── response.ts         # 统一响应
│
├── prisma.config.ts           # Prisma 自定义配置
├── package.json
└── README.md
```

---

## 预置用户

运行 `npx prisma db seed` 后，以下用户可用于登录测试：

| 用户名       | 密码           | 昵称  | 邮箱                  |
|-----------|--------------|-----|---------------------|
| `alice`   | `alice123`   | 爱丽丝 | alice@example.com   |
| `bob`     | `bob123456`  | 鲍勃  | bob@example.com     |
| `charlie` | `charlie123` | 查理  | charlie@example.com |
| `admin`   | `admin123`   | 管理员 | admin@netmusic.com  |

---

## 常用命令

```bash
npm run dev      # 启动开发服务器（热重载）
npm run build    # TypeScript 编译
npm start        # 启动生产构建
npm run lint     # 代码检查
npm test         # 运行测试
npx prisma studio          # 打开 Prisma 数据库管理界面（浏览器）
npx prisma migrate dev     # 创建并应用迁移
npx prisma db seed         # 播种测试数据
npx prisma migrate reset   # 重置数据库
npx tsc --noEmit           # TypeScript 类型检查（不输出文件）
```