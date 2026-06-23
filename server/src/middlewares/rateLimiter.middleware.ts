import rateLimit from 'express-rate-limit';

// 通用 API 限流：每 IP 每分钟最多 100 次请求
export const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 分钟窗口
  max: 100,
  message: { code: 429, message: '请求过于频繁，请稍后再试', data: null },
  standardHeaders: true,
  legacyHeaders: false,
});

// 认证接口限流（登录/注册）：每 IP 每 15 分钟最多 10 次尝试
export const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 分钟窗口
  max: 10,
  message: { code: 429, message: '登录尝试过于频繁，请 15 分钟后再试', data: null },
  standardHeaders: true,
  legacyHeaders: false,
});