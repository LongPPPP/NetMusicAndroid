import prisma from '../config/database';
import {AuthErrorMessage} from '../constants/errorString';
import {ConflictError, NotFoundError, UnauthorizedError} from '../errors/AppError';
import {signToken} from '../utils/jwt';
import {comparePassword, hashPassword} from '../utils/password';
import {sanitize} from '../utils/sanitize';
import type {LoginInput, RegisterInput} from '../validators/auth.validator';

// 注册
export async function register({confirmPassword: _confirmPassword, ...data}: RegisterInput) {
    // 过滤 XSS / 特殊字符（密码不过滤，防止改掉用户原始密码）
    const username = sanitize(data.username);
    const email = sanitize(data.email);
    const {password} = data;

    // 查重 — 利用唯一索引快速失败
    const usernameExists = await prisma.user.findUnique({where: {username}, select: {id: true}});
    if (usernameExists) {
        throw new ConflictError(AuthErrorMessage.USERNAME_EXISTS);
    }

    const emailExists = await prisma.user.findUnique({where: {email}, select: {id: true}});
    if (emailExists) {
        throw new ConflictError(AuthErrorMessage.EMAIL_EXISTS);
    }

    // 加密密码
    const hashedPassword = await hashPassword(password);

    // 创建用户
    const user = await prisma.user.create({
        data: {username, password: hashedPassword, email},
    });

    // 签发 Token
    const token = signToken(user.id);
    return {userId: user.id, token};
}

// 登录
export async function login(params: LoginInput) {
    const {username, password} = params;

    const user = await prisma.user.findUnique({where: {username}});
    // 合并检查（用户不存在 / 密码错误 → 同一提示），防止枚举攻击
    if (!user || !(await comparePassword(password, user.password))) {
        throw new UnauthorizedError(AuthErrorMessage.LOGIN_FAILED);
    }

    // 签发 Token
    const token = signToken(user.id);
    return {userId: user.id, token, expiresIn: 604800};
}

// 获取用户基本信息（用于 Token 有效性验证）
export async function getUserBasicInfo(userId: number) {
    const user = await prisma.user.findUnique({
        where: {id: userId},
        select: {
            id: true,
            username: true,
            nickname: true,
            email: true,
            avatar: true,
            gender: true,
            signature: true,
        },
    });

    if (!user) {
        throw new NotFoundError(AuthErrorMessage.USER_NOT_FOUND);
    }

    return user;
}
