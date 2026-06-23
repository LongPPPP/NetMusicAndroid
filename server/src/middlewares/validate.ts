import { Request, Response, NextFunction } from 'express';
import { ZodSchema } from 'zod';
import { ValidationError } from '../errors/AppError';

/**
 * 可复用的 Zod 校验中间件。
 *
 * 使用方式：
 *   router.post('/register', validate(registerSchema), register);
 *
 * 校验成功后自动将 parsed.data 写回 req.body（类型安全）。
 * 校验失败后通过 next(new ValidationError(...)) 交给全局错误处理器。
 */
export const validate =
  (schema: ZodSchema) =>
  (req: Request, _res: Response, next: NextFunction) => {
    const result = schema.safeParse(req.body);
    if (!result.success) {
      return next(new ValidationError(result.error.issues[0].message));
    }
    // 替换为已校验的类型安全数据
    req.body = result.data;
    next();
  };