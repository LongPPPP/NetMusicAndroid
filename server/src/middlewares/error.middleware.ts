import { Request, Response, NextFunction } from 'express';
import { fail } from '../utils/response';

// 全局错误处理中间件
export function errorMiddleware(err: Error, req: Request, res: Response, next: NextFunction) {
  console.error(`[Error] ${err.message}`);
  console.error(err.stack);

  return fail(res, '服务器内部错误', 500);
}

// 404 处理
export function notFoundMiddleware(req: Request, res: Response) {
  return fail(res, `接口不存在: ${req.method} ${req.path}`, 404);
}
