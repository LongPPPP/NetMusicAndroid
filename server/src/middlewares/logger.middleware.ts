import {NextFunction, Request, Response} from 'express';

// 请求日志中间件
export function loggerMiddleware(req: Request, res: Response, next: NextFunction) {
    const start = Date.now();
    const {method, path} = req;

    res.on('finish', () => {
        const duration = Date.now() - start;
        console.log(`[${new Date().toLocaleTimeString()}] ${method} ${path} → ${res.statusCode} (${duration}ms)`);
    });

    next();
}
