import { Request, Response, NextFunction } from 'express';
import { verifyToken } from '../utils/jwt';
import { fail } from '../utils/response';

// 扩展 Express Request 类型，添加 userId
declare global {
  namespace Express {
    interface Request {
      userId?: number;
    }
  }
}

// JWT 鉴权中间件
export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    return fail(res, '未登录，请先登录', 401);
  }

  const token = authHeader.split(' ')[1]; // "Bearer xxx" → "xxx"
  if (!token) {
    return fail(res, 'Token 格式错误', 401);
  }

  try {
    const decoded = verifyToken(token);
    req.userId = decoded.userId;
    next();
  } catch (err) {
    return fail(res, 'Token 已过期或无效，请重新登录', 401);
  }
}
