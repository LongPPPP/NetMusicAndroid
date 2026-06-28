import prisma from '../config/database';
import {config} from '../config';
import {AuthErrorMessage} from '../constants/errorString';
import {ConflictError, UnauthorizedError} from '../errors/AppError';
import {signAccessToken, signRefreshToken, verifyRefreshToken} from '../utils/jwt';
import {comparePassword, hashPassword} from '../utils/password';
import {sanitize} from '../utils/sanitize';
import type {LoginInput, RegisterInput} from '../validators/auth.validator';

// 注册
export async function register({confirmPassword: _confirmPassword, ...data}: RegisterInput) {
    // 过滤 XSS / 特殊字符（密码不过滤，防止改掉用户原始密码）
    const username = sanitize(data.username);
    const email = sanitize(data.email);
    const {password} = data;

    // 邮箱查重 — 唯一标识
    const emailExists = await prisma.user.findUnique({where: {email}, select: {id: true}});
    if (emailExists) {
        throw new ConflictError(AuthErrorMessage.EMAIL_EXISTS);
    }

    // 加密密码
    const hashedPassword = await hashPassword(password);

    // 创建用户（默认角色 USER），同时创建收藏歌单
    const user = await prisma.user.create({
        data: {username, password: hashedPassword, email},
    });

    await prisma.playlist.create({
        data: {userId: user.id, name: '我的收藏', isFavorite: true},
    });
}

// 登录（使用邮箱和密码）
export async function login(params: LoginInput) {
    const {email, password} = params;

    const user = await prisma.user.findUnique({
        where: {email},
        select: {
            id: true,
            username: true,
            email: true,
            password: true,
            avatar: true,
            signature: true,
            role: true,
            createdAt: true,
            _count: {
                select: {comments: true},
            },
            playlists: {
                where: {isFavorite: true},
                select: {
                    _count: {select: {playlistSongs: true}},
                },
            },
        },
    });
    // 合并检查（用户不存在 / 密码错误 → 同一提示），防止枚举攻击
    if (!user || !(await comparePassword(password, user.password))) {
        throw new UnauthorizedError(AuthErrorMessage.LOGIN_FAILED);
    }

    const {_count, playlists, password: _, ...profile} = user;

    // 签发双 Token
    const accessToken = signAccessToken(user.id, user.role);
    const refreshToken = signRefreshToken(user.id, user.role);
    return {
        user_id: user.id,
        access_token: accessToken,
        expires_in: config.jwt.expiresIn,
        refresh_token: refreshToken,
        refresh_expires_in: config.jwt.refreshExpiresIn,
        user: {
            ...profile,
            comment_count: _count.comments,
            favorite_count: playlists[0]?._count.playlistSongs ?? 0,
        },
    };
}

// 刷新 Access Token
export async function refresh(refreshToken: string) {
    const decoded = verifyRefreshToken(refreshToken);

    // 额外校验：用户是否还存在
    const user = await prisma.user.findUnique({
        where: {id: decoded.userId},
        select: {id: true, role: true},
    });
    if (!user) {
        throw new UnauthorizedError(AuthErrorMessage.USER_NOT_FOUND);
    }

    // 签发新的 Access Token
    const newAccessToken = signAccessToken(user.id, user.role);
    return {
        access_token: newAccessToken,
        expires_in: config.jwt.expiresIn,
    };
}
