import prisma from '../config/database';
import {AuthErrorMessage} from '../constants/errorString';
import {ConflictError, NotFoundError, ValidationError} from '../errors/AppError';
import {sanitize} from '../utils/sanitize';
import {hashPassword} from '../utils/password';

/** 返回给前端的用户公开信息字段 */
const userSelect = {
    id: true,
    username: true,
    email: true,
    avatar: true,
    gender: true,
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

// 修改用户名
export async function updateUsername(userId: number, username: string) {
    const sanitized = sanitize(username);
    const updated = await prisma.user.update({
        where: {id: userId},
        data: {username: sanitized},
        select: userSelect,
    });
    return updated;
}

// 修改头像
export async function updateAvatar(userId: number, avatar: string) {
    const updated = await prisma.user.update({
        where: {id: userId},
        data: {avatar},
        select: userSelect,
    });
    return updated;
}

// 修改个性签名
export async function updateSignature(userId: number, signature: string) {
    const sanitized = sanitize(signature);
    const updated = await prisma.user.update({
        where: {id: userId},
        data: {signature: sanitized},
        select: userSelect,
    });
    return updated;
}

// 修改邮箱（唯一，需要查重）
export async function updateEmail(userId: number, email: string) {
    // 检查新邮箱是否已被其他用户占用
    const existing = await prisma.user.findFirst({
        where: {email, id: {not: userId}},
        select: {id: true},
    });
    if (existing) {
        throw new ConflictError(AuthErrorMessage.EMAIL_EXISTS);
    }

    const updated = await prisma.user.update({
        where: {id: userId},
        data: {email},
        select: userSelect,
    });
    return updated;
}

// 统一修改用户信息（数据字典：PATCH /users/me）
export async function updateUser(userId: number, field: string, value: string) {
    switch (field) {
        case 'avatar': {
            // 简单 URL 格式校验
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
        case 'password': {
            if (value.length < 6 || value.length > 20) {
                throw new ValidationError('密码长度为 6-20 个字符');
            }
            if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(value)) {
                throw new ValidationError('密码必须同时包含大小写字母和数字');
            }
            const hashed = await hashPassword(value);
            return prisma.user.update({
                where: {id: userId},
                data: {password: hashed},
                select: userSelect,
            });
        }
        default:
            throw new ValidationError('不支持修改的字段');
    }
}
