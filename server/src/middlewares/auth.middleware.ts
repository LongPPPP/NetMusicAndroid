import {NextFunction, Request, Response} from 'express';
import {UnauthorizedError} from '../errors/AppError';
import {verifyAccessToken} from '../utils/jwt';

// 扩展 Express Request 类型，添加 userId 和 userRole
declare global {
    namespace Express {
        interface Request {
            userId?: number;
            userRole?: string;
        }
    }
}

/**
 * JWT 鉴权中间件。
 *
 * 校验失败或 Token 过期时抛出 UnauthorizedError，
 * 由全局 errorMiddleware 统一处理为 "401 + 消息体" 响应。
 */
export function authMiddleware(req: Request, _res: Response, next: NextFunction) {
    const authHeader = req.headers.authorization;

    if (!authHeader) {
        return next(new UnauthorizedError('未登录，请先登录'));
    }

    const token = authHeader.split(' ')[1]; // "Bearer xxx" → "xxx"
    if (!token) {
        return next(new UnauthorizedError('Token 格式错误'));
    }

    try {
        const decoded = verifyAccessToken(token);
        req.userId = decoded.userId;
        req.userRole = decoded.role;
        next();
    } catch {
        return next(new UnauthorizedError('Token 已过期或无效，请重新登录'));
    }
}