import prisma from '../config/database';
import { hashPassword, comparePassword } from '../utils/password';
import { signToken } from '../utils/jwt';
import { sanitize } from '../utils/sanitize';
import { AuthError } from '../constants/auth.errors';
import type { RegisterInput, LoginInput } from '../validators/auth.validator';

// Prisma 唯一约束冲突错误码
const PRISMA_UNIQUE_CONSTRAINT_ERROR = 'P2002';

// 注册
export async function register(params: RegisterInput) {
  // 过滤 XSS / 特殊字符（密码不过滤，防止改掉用户原始密码）
  const username = sanitize(params.username);
  const email = sanitize(params.email);
  const { password } = params;

  // 加密密码
  const hashedPassword = await hashPassword(password);

  // 创建用户（依赖数据库唯一约束保证原子性，避免并发竞态）
  try {
    const user = await prisma.user.create({
      data: {
        username,
        password: hashedPassword,
        email,
      },
    });

    // 签发 Token
    const token = signToken(user.id);
    return {
      userId: user.id,
      token,
    };
  } catch (err: any) {
    // 捕获数据库唯一约束冲突
    if (err.code === PRISMA_UNIQUE_CONSTRAINT_ERROR) {
      // 从错误信息中判断是哪个字段冲突
      const targetField = err.meta?.target?.[0] ?? '';
      if (targetField === 'username') {
        throw new Error(AuthError.USERNAME_EXISTS);
      }
      if (targetField === 'email') {
        throw new Error(AuthError.EMAIL_EXISTS);
      }
    }
    throw err; // 其他错误继续抛出
  }
}

// 登录
export async function login(params: LoginInput) {
  const { username, password } = params;

  // 查找用户
  const user = await prisma.user.findUnique({ where: { username } });
  if (!user) {
    throw new Error(AuthError.USER_NOT_FOUND);
  }

  // 验证密码
  const valid = await comparePassword(password, user.password);
  if (!valid) {
    throw new Error(AuthError.WRONG_PASSWORD);
  }

  // 签发 Token
  const token = signToken(user.id);

  return {
    userId: user.id,
    token,
    expiresIn: 604800,
  };
}
