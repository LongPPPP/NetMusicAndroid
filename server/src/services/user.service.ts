import fs from 'fs';
import path from 'path';
import prisma from '../config/database';
import {AuthErrorMessage} from '../constants/errorString';
import {ConflictError, NotFoundError, ValidationError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';

/** 返回给前端的用户公开信息字段 */
const userSelect = {
    id: true,
    username: true,
    email: true,
    avatar: true,
    signature: true,
    role: true,
    createdAt: true,
} as const;

// 获取用户公开信息
export async function getUserById(userId: number) {
    const user = await prisma.user.findUnique({
        where: {id: userId},
        select: userSelect,
    });

    if (!user) {
        throw new NotFoundError(AuthErrorMessage.USER_NOT_FOUND);
    }

    return user;
}

// 统一修改用户信息（数据字典：PATCH /users/me）
export async function updateUser(userId: number, field: string, value: string) {
    switch (field) {
        case 'username': {
            const sanitized = sanitize(value);
            if (sanitized.length < 1 || sanitized.length > 16) {
                throw new ValidationError('用户名长度需在 1-16 个字符之间');
            }
            return prisma.user.update({
                where: {id: userId},
                data: {username: sanitized},
                select: userSelect,
            });
        }
        case 'avatar': {
            if (!/^https?:\/\/.+/.test(value)) {
                throw new ValidationError('头像地址格式不正确');
            }
            return prisma.user.update({
                where: {id: userId},
                data: {avatar: value},
                select: userSelect,
            });
        }
        case 'signature': {
            if (value.length > 100) {
                throw new ValidationError('个性签名最多 100 个字符');
            }
            const sanitized = sanitize(value);
            return prisma.user.update({
                where: {id: userId},
                data: {signature: sanitized},
                select: userSelect,
            });
        }
        case 'email': {
            // 检查新邮箱是否已被其他用户占用
            const existing = await prisma.user.findFirst({
                where: {email: value, id: {not: userId}},
                select: {id: true},
            });
            if (existing) {
                throw new ConflictError(AuthErrorMessage.EMAIL_EXISTS);
            }
            return prisma.user.update({
                where: {id: userId},
                data: {email: value},
                select: userSelect,
            });
        }
        default:
            throw new ValidationError('不支持修改的字段');
    }
}

// 上传/替换头像（含旧本地文件清理）
export async function updateAvatar(userId: number, avatarUrl: string) {
    // 查出旧头像，清理本地旧文件
    const oldUser = await prisma.user.findUnique({
        where: {id: userId},
        select: {avatar: true},
    });

    if (oldUser?.avatar?.startsWith('/static/avatars/')) {
        const oldPath = path.resolve(__dirname, '../../storage/avatars', path.basename(oldUser.avatar));
        fs.unlink(oldPath, () => {}); // 忽略删除失败
    }

    // 更新数据库
    const user = await prisma.user.update({
        where: {id: userId},
        data: {avatar: avatarUrl},
        select: userSelect,
    });

    return user;
}
