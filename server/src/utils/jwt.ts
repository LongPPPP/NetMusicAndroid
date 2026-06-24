import jwt from 'jsonwebtoken';
import {config} from '../config';

export interface TokenPayload {
    userId: number;
    role: string;
}

/** 签发 Access Token（短效，默认 15 分钟） */
export function signAccessToken(userId: number, role: string): string {
    return jwt.sign({userId, role}, config.jwt.secret, {
        expiresIn: config.jwt.expiresIn,
    });
}

/** 签发 Refresh Token（长效，默认 7 天） */
export function signRefreshToken(userId: number, role: string): string {
    return jwt.sign({userId, role}, config.jwt.refreshSecret, {
        expiresIn: config.jwt.refreshExpiresIn,
    });
}

/** 验证 Access Token */
export function verifyAccessToken(token: string): TokenPayload {
    return jwt.verify(token, config.jwt.secret) as TokenPayload;
}

/** 验证 Refresh Token */
export function verifyRefreshToken(token: string): TokenPayload {
    return jwt.verify(token, config.jwt.refreshSecret) as TokenPayload;
}
