# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
npm run dev         # Start dev server with hot reload (ts-node-dev)
npm run build       # TypeScript compile (tsc)
npm start           # Start production build
npm test            # Run tests (jest) — tests/ dir has unit/ and integration/ subdirs
npm run lint        # ESLint on src/

npx prisma migrate dev            # Create + apply DB migration
npx prisma migrate dev --name X   # Named migration after schema changes
npx prisma migrate reset          # Drop DB → re-migrate → re-seed
npx prisma db seed                 # Re-seed test data only
npx prisma generate                # Regenerate Prisma client (auto-runs on migrate)
npx prisma studio                  # Open Prisma Studio (browser DB GUI)
npx tsc --noEmit                   # Type check only (no output)
```

## Architecture

### Layer pattern (per domain, e.g. `auth`, `user`)

```
routes/       →  HTTP routing, middleware wiring (auth, validate, rate-limit)
controllers/  →  Request handling, response formatting (asyncHandler 包裹，无 try-catch)
services/     →  Business logic, Prisma queries, 抛出类型化错误
validators/   →  Zod schemas + inferred TypeScript types for request bodies
errors/       →  自定义错误类（AppError 基类 + 子类），每个自带 HTTP 状态码
```

### Project structure

```
src/
  index.ts                    # Express app entry: global middleware, routes, error handlers
  config/
    index.ts                  # Environment config (port, JWT secret/expiry)
    database.ts               # PrismaClient singleton with libSQL adapter (SQLite)
  routes/
    index.ts                  # Aggregates all route modules under /api/v1
    auth.routes.ts            # POST /register, /login, /verify-token
    user.routes.ts            # GET /:id, PUT /:id
  controllers/
    auth.controller.ts        # Register / Login / Verify-token handlers
    user.controller.ts        # Get profile / Update profile handlers
  errors/
    AppError.ts               # AppError 基类 + 5 子类（Validation/Unauthorized/Forbidden/NotFound/Conflict）
  services/
    auth.service.ts           # Registration, login, token verification
    user.service.ts           # Get/update user profile
  validators/
    auth.validator.ts         # registerSchema, loginSchema
    user.validator.ts         # updateUserSchema
  middlewares/
    auth.middleware.ts        # JWT verification, injects req.userId
    error.middleware.ts       # Global error handler（instanceof AppError）+ 404 handler
    logger.middleware.ts      # Request logging with duration
    rateLimiter.middleware.ts # Rate limiting (apiLimiter: 100/min, authLimiter: 10/15min)
    validate.ts               # Zod 校验中间件，失败 → next(new ValidationError)
  utils/
    asyncHandler.ts           # 包裹异步路由，自动 catch → next(err)
    jwt.ts                    # signToken / verifyToken (jsonwebtoken)
    password.ts               # hashPassword / comparePassword (bcryptjs)
    response.ts               # success(data) / fail(message) — unified JSON shape
    sanitize.ts               # XSS filtering (strip all HTML tags)
    errorString.ts            # AuthErrorMessage — 统一用户提示消息（已移除了 AuthError 枚举）
prisma/
  schema.prisma               # Data model: User only (SQLite)
  seed.ts                     # 4 test users (alice, bob, charlie, admin)
```

### Key patterns

- **Routing**: All routes mounted under `/api/v1` in `routes/index.ts`. Route files wire middleware + controller
  functions.
- **Validation**: Zod schemas in `validators/` — parsed in routes via `validate(schema)` middleware. First error message
  returned to client. Controller 不再直接调用 `.safeParse()`。
- **Error handling**: Services throw typed errors (`ConflictError`, `NotFoundError`, `UnauthorizedError` 等，定义在
  `errors/AppError.ts`）。Controller 用 `asyncHandler` 包裹，自动将异常传给全局 `errorMiddleware`，后者通过
  `instanceof AppError` 区分已知/未知错误。Controller 中无需 try-catch。
- **Response format**: Every response uses `success(res, data, message, code)` or `fail(res, message, code)`. Shape:
  `{ code, message, data }`.
- **Auth flow**: JWT-based. `authMiddleware` extracts Bearer token, verifies, sets `req.userId`. `POST /verify-token`
  enables auto-login on app start.
- **Rate limiting**: Auth endpoints (register/login) limited to 10 requests per 15 min per IP.
- **Database**: Prisma 7 + SQLite via libSQL adapter. Schema lives in `prisma/schema.prisma`, generated client in
  `src/generated/prisma/`. The `prisma.config.ts` file (Prisma 7 config) manages datasource URL and seed path.
- **New domain pattern**: Create `routes/X.routes.ts` → `controllers/X.controller.ts` → `services/X.service.ts` →
  `validators/X.validator.ts` (if needed). If custom error types are needed, export from `errors/AppError.ts`. Register
  routes in `routes/index.ts`.

### Database schema

Only one model currently: `User` (id, username, password, nickname, email, avatar, signature, timestamps).
SQLite via Prisma with libSQL adapter.

### Git workflow

- Current branch: `server` (main work branch)
- Master branch: `master`
- Prisma-generated files in `src/generated/prisma/` are gitignored (regenerate after pull)

## 代码规范

参考本项目中已有的模块进行新模块的设计，统一代码风格。

- 缩进使用4个空格
- 错误类型使用 `errors/AppError.ts` 中的类（`ConflictError` / `NotFoundError` / `ValidationError` 等），每个自带 HTTP
  状态码，严禁魔法字符串。
- Controller 用 `asyncHandler` 包裹，不要手写 try-catch。
- 路由层用 `validate(schema)` 中间件做请求体校验，controller 中不要调用 `.safeParse()`。
- 用户提示消息（中文文案）统一放在 `constants/errorString.ts` 的 `AuthErrorMessage` 中。
- 一定要检查文档是否过期，在设计/修改完 api 后，在 `docs/` 中生成/修改对应的接口文档。
- Zod 校验注意：`z.string().email()` / `z.string().url()` / `z.string().jwt()` 已弃用，改用顶层的 `z.email()` / `z.url()` / `z.jwt()`。这些顶层方法也支持 `.trim()` / `.min()` / `.max()` 等链式调用。
