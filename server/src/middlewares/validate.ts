import {NextFunction, Request, Response} from 'express';
import {ZodSchema} from 'zod';
import {ValidationError} from '../errors/AppError';

/**
 * 可复用的 Zod 校验中间件（校验 req.body）。
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
            req.body = result.data;
            next();
        };

/**
 * 校验 req.query 参数（用于 GET 请求的查询参数校验）。
 *
 * 使用方式：
 *   router.get('/songs', validateQuery(getSongsSchema), listSongs);
 *
 * 校验成功后 parsed.data 挂在 req.query 上。
 */
export const validateQuery =
    (schema: ZodSchema) =>
        (req: Request, _res: Response, next: NextFunction) => {
            const result = schema.safeParse(req.query);
            if (!result.success) {
                return next(new ValidationError(result.error.issues[0].message));
            }
            // Express 5 中 req.query 是只读 getter，需用 defineProperty 替换
            Object.defineProperty(req, 'query', {
                value: result.data,
                writable: true,
                configurable: true,
            });
            next();
        };
