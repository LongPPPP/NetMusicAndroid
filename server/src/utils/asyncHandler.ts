import {NextFunction, Request, RequestHandler, Response} from 'express';

/**
 * 包裹异步路由处理器，自动将 rejected promise 转发给 Express 错误中间件。
 *
 * 使用方式：
 *   export const getProfile = asyncHandler(async (req, res) => { ... });
 *
 * 不再需要在每个 controller 里写 try-catch。
 */
export const asyncHandler =
    (fn: RequestHandler): RequestHandler =>
        (req: Request, res: Response, next: NextFunction) =>
            Promise.resolve(fn(req, res, next)).catch(next);