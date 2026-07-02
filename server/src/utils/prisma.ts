import {PrismaClientKnownRequestError} from '@prisma/client/runtime/client';
import {ConflictError, NotFoundError} from '../errors/AppError';

interface PrismaErrorMap {
    P2002?: string; // 唯一约束冲突 → ConflictError
    P2003?: string; // 外键约束冲突 → NotFoundError
    P2025?: string; // 记录不存在 → NotFoundError
}

/**
 * 柯里化的 Prisma 错误处理器，用于 `.catch()` 链式调用。
 * 将已知 Prisma 错误码映射为类型化 AppError。
 *
 * 使用方式：
 *   prisma.user.create({ data }).catch(prismaError({ P2002: '邮箱已被注册' }));
 */
export function prismaError(map: PrismaErrorMap) {
    return (err: unknown) => {
        if (err instanceof PrismaClientKnownRequestError) {
            if (err.code === 'P2002' && map.P2002) throw new ConflictError(map.P2002);
            if (err.code === 'P2003' && map.P2003) throw new NotFoundError(map.P2003);
            if (err.code === 'P2025' && map.P2025) throw new NotFoundError(map.P2025);
        }
        throw err;
    };
}

/**
 * 类型守卫：判断错误是否为特定 Prisma 错误码。
 * 用于需要分支逻辑（非简单 throw）的场景。
 *
 * 使用方式：
 *   if (isPrismaCode(err, 'P2002')) { ... }
 */
export function isPrismaCode(err: unknown, code: string): err is PrismaClientKnownRequestError {
    return err instanceof PrismaClientKnownRequestError && err.code === code;
}
