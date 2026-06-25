import {NextFunction, Request, Response} from 'express';
import {ForbiddenError} from '../errors/AppError';

/**
 * 角色守卫中间件。
 *
 * 限制接口仅允许指定角色的用户访问，需在 authMiddleware 之后使用。
 *
 * 使用方式：
 *   router.delete('/songs/:id', authMiddleware, requireRole('ADMIN'), songController.delete);
 *   router.post('/songs', authMiddleware, requireRole('ARTIST', 'ADMIN'), songController.create);
 */
export function requireRole(...roles: string[]) {
    return (req: Request, _res: Response, next: NextFunction) => {
        if (!req.userRole || !roles.includes(req.userRole)) {
            return next(new ForbiddenError('权限不足，无法执行此操作'));
        }
        next();
    };
}
