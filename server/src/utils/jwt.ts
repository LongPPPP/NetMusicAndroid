import jwt from 'jsonwebtoken';
import { config } from '../config';

interface TokenPayload {
  userId: number;
}

// 签发 Token
export function signToken(userId: number): string {
  return jwt.sign({ userId }, config.jwt.secret, {
    expiresIn: config.jwt.expiresIn,
  });
}

// 验证 Token
export function verifyToken(token: string): TokenPayload {
  return jwt.verify(token, config.jwt.secret) as TokenPayload;
}
