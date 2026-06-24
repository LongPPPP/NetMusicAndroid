import {NextFunction, Request, Response} from 'express';
import jwt from 'jsonwebtoken';
import {AppError} from '../errors/AppError';
import {fail} from '../utils/response';

/**
 * 全局错误处理中间件 — 只写一次，所有 controller 和 service 共用。
 *
 * - AppError 及其子类 → 提取 err.message + err.statusCode 直接返回
 * - JsonWebToken 异常 → 返回 401（统一映射，service 无需再手动转换）
 * - 其他未预期错误 → 记日志，返回 500 服务器内部错误
 */
export function errorMiddleware(err: Error, req: Request, res: Response, _next: NextFunction) {
    if (err instanceof AppError) {
        return fail(res, err.message, err.statusCode);
    }

    // JWT 异常统一返回 401（jsonwebtoken 原生错误不属于 AppError）
    if (err instanceof jwt.JsonWebTokenError || err instanceof jwt.TokenExpiredError) {
        return fail(res, 'Token 已过期或无效，请重新登录', 401);
    }

    console.error(`[${req.method} ${req.path}]`, err);
    return fail(res, '服务器内部错误', 500);
}

// 404 处理
export function notFoundMiddleware(req: Request, res: Response) {
    return fail(res, `接口不存在: ${req.method} ${req.path}`, 404);
}